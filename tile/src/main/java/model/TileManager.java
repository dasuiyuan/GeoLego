package model;

import helper.BaseTileCalculator;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.sylab.geolego.index.rtree.RTreeIndexOper;
import util.ProjectUtil;
import helper.TileCalculatorWGS84;
import util.SpatialUtil;

import java.util.*;

import static util.SpatialUtil.toPolygon;

/**
 * @author : suiyuan
 * @description : 瓦片管理工具
 * @date : Created in 2020-04-24 10:50
 * @modified by :
 **/
public class TileManager {

    /**
     * 道格拉斯化简算法距离容差
     */
    private final static double SIMPLIFY_DISTANCE_TOLARENCE = 0.02197265625;

    /**
     * 瓦片像素范围
     */
    private int extent;

    private BaseTileCalculator tileCalculator;

    /**
     * Tile只装载geometry，不做clip、simplify处理
     */
    private boolean justLoadGeom = false;

    public TileManager(int extent) {
        this.extent = extent;
        this.tileCalculator = new TileCalculatorWGS84(extent);
    }

    /**
     * 获取指定范围内各级别的瓦片
     *
     * @param startLevel 起始级别
     * @param endLevel   中止级别
     * @param envelope   地理范围
     * @return 各级别瓦片字典
     */
    public Map<Integer, List<TilePiece>> getEmptyTiles(int startLevel, int endLevel, Envelope envelope) {
        if (startLevel <= 0 || startLevel > endLevel) {
            return null;
        }
        Map<Integer, List<TilePiece>> tileDic = new HashMap<>(endLevel - startLevel + 1);
        for (int i = startLevel; i < endLevel + 1; i++) {
            tileDic.put(i, getTiles(i, envelope));
        }
        return tileDic;
    }

    /**
     * 根据瓦片起止级别、地理范围生成几何对象集合对应每个级别的所有瓦片
     *
     * @param startLevel 起始级别
     * @param endLevel   中止级别
     * @param envelope   地理范围
     * @param geometries 几何对象集合
     * @return 各级别的瓦片集合
     */
    public Map<Integer, List<TilePiece>> createTiles(int startLevel, int endLevel, Envelope envelope, List<Geometry> geometries) {
        Map<Integer, List<TilePiece>> tileMap = new HashMap<>(endLevel - startLevel + 1);
        for (int i = startLevel; i <= endLevel; i++) {
            List<TilePiece> tilePieces = createTiles(i, envelope, geometries);
            if (tilePieces != null) {
                tileMap.put(i, tilePieces);
            }
        }
        return tileMap;
    }

    /**
     * 根据级别、地理范围生成几何对象集合对应的所有瓦片
     *
     * @param level      瓦片级别
     * @param envelope   地理范围
     * @param geometries 几何对象集合
     * @return 瓦片集合
     */
    public List<TilePiece> createTiles(int level, Envelope envelope, List<Geometry> geometries) {
        RTreeIndexOper rTreeIndexOper = new RTreeIndexOper(geometries);
        rTreeIndexOper.buildIndex();
        //获取所有瓦片块
        List<TilePiece> tilePieces = getTiles(level, envelope);
        //遍历tilePieces，用mbr切割geometry
        tilePieces.forEach(tilePiece -> {
            Geometry mbrPlg = toPolygon(tilePiece.getMbr(), geometries.get(0).getSRID());
            List<Geometry> intersects = rTreeIndexOper.searchIntersect(mbrPlg, false);
            if (intersects != null) {
                List<Geometry> clippedGeometries = new ArrayList<>(intersects.size());
                if (justLoadGeom) {
                    clippedGeometries = intersects;
                } else {
                    for (Geometry geometry : intersects) {
                        try {
                            Geometry intersection = geometry;
                            if (!mbrPlg.contains(geometry)) {
                                intersection = SpatialUtil.clip(geometry, tilePiece.getMbr());
                            }
                            //道格拉斯化简
                            Geometry simplifiedGeom = TopologyPreservingSimplifier.simplify(intersection, SIMPLIFY_DISTANCE_TOLARENCE);

                            clippedGeometries.add(simplifiedGeom);
                        } catch (Exception ex) {
                            System.out.println(ex);
                        }
                    }
                }
                tilePiece.setGeometries(clippedGeometries);
            }
        });
        return tilePieces;
    }

    /**
     * 根据级别、地理范围生成几何对象对应瓦片集合
     *
     * @param level    瓦片级别
     * @param envelope 地理范围
     * @param geometry 几何对象
     * @return 瓦片集合
     */
    public List<TilePiece> createTiles(int level, Envelope envelope, Geometry geometry) {
        List<TilePiece> tilePieces = getTiles(level, envelope);
        //遍历tilePieces，用mbr切割geometry
        tilePieces.forEach(tilePiece -> {
            Geometry mbrPlg = toPolygon(tilePiece.getMbr(), geometry.getSRID());
            if (mbrPlg.intersects(geometry)) {
                //道格拉斯化简
                Geometry simpleGeom = TopologyPreservingSimplifier.simplify(geometry, 0.003);
                Geometry intersection = mbrPlg.intersection(simpleGeom);
                tilePiece.setGeometries(Collections.singletonList(intersection));
            }
        });
        return tilePieces;
    }

    /**
     * 创建指定点在对应级别中的瓦片
     *
     * @param level 瓦片级别
     * @param lng   经度
     * @param lat   纬度
     * @return 瓦片
     */
    public TilePiece getTile(int level, double lng, double lat) {
        double[] coordDelta = tileCalculator.getCoordDelta(level);
        int colIndex = (int) (lng / coordDelta[0]);
        int rowIndex = (int) (lat / coordDelta[1]);
        Envelope mbr = tileCalculator.getCoordMBR(level, colIndex, rowIndex);
        TilePiece tilePiece = new TilePiece(level, colIndex, rowIndex);
        return tilePiece.setMbr(mbr);
    }


    /**
     * 根据级别和地理范围获取瓦片集合
     *
     * @param level
     * @param envelope
     * @return
     */
    public List<TilePiece> getTiles(int level, Envelope envelope) {
        int[] indexMin = tileCalculator.getTileIndex(level, envelope.getMinX(), envelope.getMinY());
        int[] indexMax = tileCalculator.getTileIndex(level, envelope.getMaxX(), envelope.getMaxY());

        int colIndexMin = indexMin[0];
        int colIndexMax = indexMax[0];
        int rowIndexMin = indexMin[1];
        int rowIndexMax = indexMax[1];

        List<TilePiece> tilePieces = new ArrayList<>((colIndexMax - colIndexMin + 1) * (rowIndexMax - rowIndexMin + 1));

        for (int i = colIndexMin; i <= colIndexMax; i++) {
            for (int j = rowIndexMin; j <= rowIndexMax; j++) {
                Envelope mbr = tileCalculator.getCoordMBR(level, i, j);
                TilePiece tilePiece = new TilePiece(level, i, j);
                tilePieces.add(tilePiece.setMbr(mbr));
            }
        }
        return tilePieces;
    }

    /**
     * 将瓦片中要素坐标转换为瓦片像素坐标
     *
     * @param tilePiece 瓦片块
     * @return
     */
    public List<Geometry> getProjectedGeoms(TilePiece tilePiece) {
        List<Geometry> geometries = tilePiece.getGeometries();
        if (geometries != null) {
            double[] coordDelta = tileCalculator.getCoordDelta(tilePiece.getLevel());
            List<Geometry> prjGeoms = new ArrayList<>(geometries.size());
            for (Geometry geometry : geometries) {
                if (geometry instanceof Polygon) {
                    Polygon prjPlg = ProjectUtil.project((Polygon) geometry, this.extent, tilePiece.getMbr(), coordDelta[0], coordDelta[1]);
                    prjGeoms.add(prjPlg);
                } else if (geometry instanceof MultiPolygon) {
                    MultiPolygon multiPolygon = (MultiPolygon) geometry;
                    Polygon[] prjPlg = new Polygon[multiPolygon.getNumGeometries()];
                    for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                        Polygon plg = (Polygon) multiPolygon.getGeometryN(i);
                        prjPlg[i] = ProjectUtil.project(plg, this.extent, tilePiece.getMbr(), coordDelta[0], coordDelta[1]);
                    }
                    MultiPolygon prjMutiPlg = geometry.getFactory().createMultiPolygon(prjPlg);
                    prjGeoms.add(prjMutiPlg);
                }
            }
            return prjGeoms;
        }
        return null;
    }

    public void outputTiles(String file, List<TilePiece> tilePieces) {

    }

    public boolean isJustLoadGeom() {
        return justLoadGeom;
    }

    public void setJustLoadGeom(boolean justLoadGeom) {
        this.justLoadGeom = justLoadGeom;
    }
}

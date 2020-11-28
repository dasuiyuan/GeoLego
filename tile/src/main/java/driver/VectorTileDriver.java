package driver;

import com.wdtinc.mapbox_vector_tile.adapt.jts.MvtEncoder;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsLayer;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsMvt;
import model.TileManager;
import model.TilePiece;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.sylab.geolego.io.helper.GeoReader;
import org.sylab.geolego.io.helper.GeoWriter;
import util.FileUtils;
import util.SpatialUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

/**
 * @author : suiyuan
 * @description : 矢量瓦片生成器
 * @date : Created in 2020-04-29 14:48
 * @modified by :
 **/
public class VectorTileDriver {
    public static void main(String[] args) throws IOException {
        createTileByGeometries("E:\\work\\files\\JUST\\GIS\\data\\XZQ84\\province_Project.shp", "E:\\work\\files\\JUST\\GIS\\data\\tilegrid\\wgs84");
    }

    private static void createTileByGeometries(String shpfile, String outFolder) throws IOException {
        List<Geometry> geometries = GeoReader.ReadShp(shpfile);
        Envelope env = new Envelope(116, 135, 42, 56);
        TileManager tileManager = new TileManager(4096);
        Map<Integer, List<TilePiece>> tilePieces = tileManager.createTiles(4, 6, env, geometries);
        if (tilePieces != null) {


            tilePieces.entrySet().forEach(entry -> {
                int level = entry.getKey();
                //输出每个级别瓦片范围wkt
                List<Geometry> levelGrid = entry.getValue().stream().map(tile -> SpatialUtil.toPolygon(tile.getMbr(), 4326)).collect(Collectors.toList());
                GeoWriter.WriteWKT(levelGrid, outFolder + File.separator + level + ".wkt");


//                entry.getValue().forEach(tilePiece -> {
//                    File colDir = new File(outFolder + File.separator + level + File.separator + tilePiece.getColNum());
//                    colDir.mkdirs();
//                    String pbf = colDir.getPath() + File.separator + tilePiece.getRowNum() + ".pbf";
//                    byte[] encoded = null;
//                    if (tilePiece.getGeometries() != null) {
//
////                        GeoWriter.WriteWKT(tilePiece.getGeometries(), "E:\\work\\files\\JUST\\GIS\\data\\tile\\province_tile_noprj.wkt");
//
//
//                        //todo: 问题就出在地理转屏幕坐标
//                        JtsLayer layer = new JtsLayer("province", tileManager.getProjectedGeoms(tilePiece), 4096);
//                        JtsMvt mvt = new JtsMvt(singletonList(layer));
//                        encoded = MvtEncoder.encode(mvt);
//                    }
//                    FileUtils.write2File(pbf, encoded);
//                });
            });
        }
    }

    private static void test() throws IOException {
        List<Geometry> geometries = GeoReader.ReadShp("E:\\work\\files\\JUST\\GIS\\data\\XZQ84\\province_Project.shp");
        Geometry hlj = geometries.stream().filter(geom -> geom.getUserData().equals("province_Project.7")).findFirst().get();
        System.out.println(hlj.toText());
        TileManager tileManager = new TileManager(4096);
        List<TilePiece> tilePieces = tileManager.createTiles(7, new Envelope(132, 135, 47, 49), hlj);


        List<Geometry> tileGeoms = tilePieces.stream().filter(tilePiece -> tilePiece.getGeometries() != null).map(tilePiece -> tilePiece.getGeometries().get(0)).collect(Collectors.toList());
        //GeoWriter.WriteWKT(tileGeoms, "E:\\work\\files\\JUST\\GIS\\data\\13_06\\hlj_geom.wkt");
        for (TilePiece tilePiece : tilePieces) {
            byte[] encoded = null;
            if (tilePiece.getGeometries() != null) {
//                GeoWriter.WriteWKT(tilePiece.getGeometries(), String.format("E:\\work\\files\\JUST\\GIS\\data\\13_06_new\\%04d_%04d.wkt", tilePiece.getColNum(), tilePiece.getRowNum()));
                JtsLayer layer = new JtsLayer("province", tileManager.getProjectedGeoms(tilePiece), 4096);
                JtsMvt mvt = new JtsMvt(singletonList(layer));
                encoded = MvtEncoder.encode(mvt);
            }
            FileUtils.write2File(String.format("E:\\work\\files\\JUST\\GIS\\data\\13_06_new\\%04d_%04d.pbf", tilePiece.getColNum(), tilePiece.getRowNum()), encoded);
        }
    }
}

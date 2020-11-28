package helper;

import org.locationtech.jts.geom.Envelope;

/**
 * @author : suiyuan
 * @description : 瓦片计算工具（只与瓦片索引划分标准有关，如：WGS84瓦片（天地图）、Web Mercaor瓦片（google））
 * @date : Created in 2020-04-24 09:17
 * @modified by :
 **/
public class TileCalculatorWGS84 extends BaseTileCalculator {

    public TileCalculatorWGS84(int tileExtent) {
        this.tileExtent = tileExtent;
    }

    /**
     * 获取经纬度单位步长
     *
     * @param level 级别
     * @return 经纬度单位步长
     */
    @Override
    public double[] getCoordDelta(int level) {
        int[] tileCount = getTileCount(level);
        int colCount = tileCount[0];
        int rowCount = tileCount[1];
        double[] coordDelta = new double[2];
        double lngDelta = 360.0 / colCount;
        double latDelta = 180.0 / rowCount;
        coordDelta[0] = lngDelta;
        coordDelta[1] = latDelta;
        return coordDelta;
    }

    /**
     * 获取指定行列号的坐标范围（左下角原点）
     *
     * @param level    级别
     * @param colIndex 列号
     * @param rowIndex 行号
     * @return 地理范围
     */
    @Override
    public Envelope getCoordMBR(int level, int colIndex, int rowIndex) {
        double[] coordDelta = getCoordDelta(level);
        double lngMin = coordDelta[0] * colIndex - 180;
        double latMin = coordDelta[1] * rowIndex - 90;
        double lngMax = lngMin + coordDelta[0];
        double latMax = latMin + coordDelta[1];
        return new Envelope(lngMin, lngMax, latMin, latMax);
    }

    /**
     * 根据级别和经纬度获取对应行列号
     *
     * @param level 级别
     * @param lng   经度
     * @param lat   纬度
     * @return
     */
    @Override
    public int[] getTileIndex(int level, double lng, double lat) {
        double[] coordDelta = getCoordDelta(level);
        int colIndex = (int) Math.floor((lng + 180) / coordDelta[0]);
        int rowIndex = (int) Math.floor((lat + 90) / coordDelta[1]);
        int[] tileIndex = new int[2];
        tileIndex[0] = colIndex;
        tileIndex[1] = rowIndex;
        return tileIndex;
    }

    /**
     * 根据瓦片级别、行列号、经纬度获取点在对应瓦片中的像素坐标
     *
     * @param level    瓦片级别
     * @param colIndex 列号
     * @param rowIndex 行号
     * @param lng      经度
     * @param lat      纬度
     * @return 像素坐标
     */
    @Override
    public double[] tileCoord(int level, int colIndex, int rowIndex, double lng, double lat) {
        Envelope mbr = getCoordMBR(level, colIndex, rowIndex);
        double[] coordDelta = getCoordDelta(level);
        double tileX = Math.floor((lng - mbr.getMinX()) * (this.tileExtent / coordDelta[0]));
        double tileY = Math.floor((lat - mbr.getMinY()) * (this.tileExtent / coordDelta[1]));
        double[] tileCoord = new double[2];
        tileCoord[0] = tileX;
        tileCoord[1] = tileY;
        return tileCoord;
    }

    /**
     * 获取行列数
     *
     * @param level 瓦片级别
     * @return 瓦片行列数
     */
    private int[] getTileCount(int level) {
        int[] tileCount = new int[2];
        int colCount = (int) (2 * Math.pow(2, level));
        int rowCount = (int) Math.pow(2, level);
        tileCount[0] = colCount;
        tileCount[1] = rowCount;
        return tileCount;
    }


}

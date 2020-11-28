package helper;

import org.locationtech.jts.geom.Envelope;

/**
 * @author : suiyuan
 * @description : 瓦片计算基类
 * @date : Created in 2020-04-29 14:51
 * @modified by :
 **/
public abstract class BaseTileCalculator {
    /**
     * 瓦片像素范围
     */
    protected int tileExtent;

    /**
     * 获取经纬度单位步长
     *
     * @param level 级别
     * @return 经纬度单位步长
     */
    public abstract double[] getCoordDelta(int level);

    /**
     * 获取指定行列号的坐标范围
     *
     * @param level    级别
     * @param colIndex 列号
     * @param rowIndex 行号
     * @return 地理范围
     */
    public abstract Envelope getCoordMBR(int level, int colIndex, int rowIndex);

    /**
     * 根据级别和经纬度获取对应行列号
     *
     * @param level 级别
     * @param lng   经度
     * @param lat   纬度
     * @return
     */
    public abstract int[] getTileIndex(int level, double lng, double lat);

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
    public abstract double[] tileCoord(int level, int colIndex, int rowIndex, double lng, double lat);
}

package helper;

import org.locationtech.jts.geom.Envelope;

/**
 * @author : suiyuan
 * @description : 墨卡托切片标准瓦片计算器
 * @date : Created in 2020-04-29 16:27
 * @modified by :
 **/
public class TileCalculatorWebMercator extends BaseTileCalculator {
    @Override
    public double[] getCoordDelta(int level) {
        return new double[0];
    }

    @Override
    public Envelope getCoordMBR(int level, int colIndex, int rowIndex) {
        return null;
    }

    @Override
    public int[] getTileIndex(int level, double lng, double lat) {
        return new int[0];
    }

    @Override
    public double[] tileCoord(int level, int colIndex, int rowIndex, double lng, double lat) {
        return new double[0];
    }
}

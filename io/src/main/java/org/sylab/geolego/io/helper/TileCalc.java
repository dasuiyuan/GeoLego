package org.sylab.geolego.io.helper;

/**
 * @author : suiyuan
 * @description :
 * @date : Created in 2020-04-23 17:29
 * @modified by :
 **/
public class TileCalc {

    public static void main(String[] args) {
        TileCalc tileCalc = new TileCalc();
        double x = tileCalc.longitudeToPixelX(113.5, 6);
        System.out.println(x);
    }

    int TILE_SIZE = 4096;

    /*
     * 获取指定级别的瓦片数目
     */
    public double _getMapSize(int level) {
        return Math.pow(2, level);
    }

    public double longitudeToTileX(double longitude, int zoom) {
        double px = this.longitudeToPixelX(longitude, zoom);
        return this.pixelXToTileX(px, zoom);
    }

    /**
     * Convert a latitude coordinate (in degrees) to a tile Y number at a
     * certain zoom level.纬度转瓦片行号
     *
     * @param latitude the latitude coordinate that should be converted.
     * @param zoom     the zoom level at which the coordinate should be converted.
     * @return the tile Y number of the latitude value.
     */
    public double latitudeToTileY(double latitude, int zoom) {
        double py = this.latitudeToPixelY(latitude, zoom);
        return this.pixelYToTileY(py, zoom);
    }

    /**
     * Convert a latitude coordinate (in degrees) to a pixel Y coordinate at a
     * certain zoom level.经纬度坐标(纬度)转屏幕像素坐标(Y)
     *
     * @param latitude the latitude coordinate that should be converted.
     * @param zoom     the zoom level at which the coordinate should be converted.
     * @return the pixel Y coordinate of the latitude value.
     */
    public double latitudeToPixelY(double latitude, int zoom) {
        double sinLatitude = Math.sin(latitude * Math.PI / 180);
        return (0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI)) * (this.TILE_SIZE << zoom);
    }

    /**
     * Convert a longitude coordinate (in degrees) to a pixel X coordinate at a
     * certain zoom level.经纬度坐标(经度)转屏幕像素坐标(X)
     *
     * @param longitude the longitude coordinate that should be converted.
     * @param zoom      the zoom level at which the coordinate should be converted.
     * @return the pixel X coordinate of the longitude value.
     */
    public double longitudeToPixelX(double longitude, int zoom) {
        return (longitude + 180) / 360 * (this.TILE_SIZE << zoom);
    }

    /*
    * 指定级别下，将宏观上的经度转换为对应列上的瓦片的像素矩阵列号(微观)
      例如：鼠标点击地图，鼠标位置在点击的瓦片内的行列号(即使像素行列号)。根据该瓦片(png)和计算出的行列号，即可取得鼠标点击位置的像素值。
   */
    public double _lngToPixelX(double longitude, int level) {
        double x = (longitude + 180) / 360;
        double pixelX = Math.floor(x * this._getMapSize(level) * 256 % 256);
        return pixelX;
    }

    /*
     * 指定级别纬度对应的像素行号
     */
    public double _latToPixelY(double latitude, int level) {
        double sinLatitude = Math.sin(latitude * Math.PI / 180);
        double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);
        double pixelY = Math.floor(y * this._getMapSize(level) * 256 % 256);
        return pixelY;
    }

    /**
     * Convert a pixel X coordinate to the tile X number.
     * 像素坐标X转瓦片行列号X
     *
     * @param pixelX the pixel X coordinate that should be converted.
     * @param zoom   the zoom level at which the coordinate should be converted.
     * @return the tile X number.
     */
    public double pixelXToTileX(double pixelX, int zoom) {
        return Math.floor(Math.min(Math.max(pixelX / this.TILE_SIZE, 0), Math.pow(2, zoom) - 1));
    }

    /**
     * Converts a pixel Y coordinate to the tile Y number.
     * 像素坐标Y转瓦片行列号Y
     *
     * @param pixelY the pixel Y coordinate that should be converted.
     * @param zoom   the zoom level at which the coordinate should be converted.
     * @return the tile Y number.
     */
    public double pixelYToTileY(double pixelY, int zoom) {
        return Math.floor(Math.min(Math.max(pixelY / this.TILE_SIZE, 0), Math.pow(2, zoom) - 1));
    }
}

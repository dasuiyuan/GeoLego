package org.sylab.geolego.model.utils;

/**
 * @author Sui Yuan
 * @description
 * @date 2021/2/4 16:23
 */
public class CoordTransformUtils {


    /**
     * 地球长半径
     */
    public static final double EARTH_RADIUS_IN_METER = 6378137.0;

    /**
     * 扁率
     */
    public static final double FLATTENING = 0.00669342162296594323;


    /**
     * 角度转化为PI的3000倍
     */
    private static final double X_PI = Math.PI * 3000.0 / 180.0;

    /**
     * 百度坐标系(BD-09)转WGS坐标
     *
     * @param lng 百度坐标纬度
     * @param lat 百度坐标经度
     * @return WGS84坐标数组
     */
    public static double[] bd09Towgs84(double lng, double lat) {
        double[] gcj = bd09Togcj02(lng, lat);
        return gcj02Towgs84(gcj[0], gcj[1]);
    }

    /**
     * WGS坐标转百度坐标系(BD-09)
     *
     * @param lng WGS84坐标系的经度
     * @param lat WGS84坐标系的纬度
     * @return 百度坐标数组
     */
    public static double[] wgs84Tobd09(double lng, double lat) {
        double[] gcj = wgs84Togcj02(lng, lat);
        return gcj02Tobd09(gcj[0], gcj[1]);
    }

    /**
     * 火星坐标系(GCJ-02)转百度坐标系(BD-09)
     *
     * @param lng 火星坐标经度
     * @param lat 火星坐标纬度
     * @return 百度坐标数组
     * @See 谷歌、高德——>百度
     */
    public static double[] gcj02Tobd09(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * X_PI);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * X_PI);
        double bdLng = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        return new double[]{bdLng, bdLat};
    }

    /**
     * 百度坐标系(BD-09)转火星坐标系(GCJ-02)
     *
     * @param lng 百度坐标纬度
     * @param lat 百度坐标经度
     * @return 火星坐标数组
     * @See 百度——>谷歌、高德
     */
    public static double[] bd09Togcj02(double lat, double lng) {
        double x = lat - 0.0065;
        double y = lng - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        double ggLng = z * Math.cos(theta);
        double ggLat = z * Math.sin(theta);
        return new double[]{ggLng, ggLat};
    }

    /**
     * WGS84转GCJ02(火星坐标系)
     *
     * @param lng WGS84坐标系的经度
     * @param lat WGS84坐标系的纬度
     * @return 火星坐标数组
     */
    public static double[] wgs84Togcj02(double lng, double lat) {
        double[] delta = calDelta(lng, lat);
        return new double[]{lng + delta[0], lat + delta[1]};
    }

    /**
     * GCJ02(火星坐标系)转GPS84
     *
     * @param lng 火星坐标系的经度
     * @param lat 火星坐标系纬度
     * @return WGS84坐标数组
     */
    public static double[] gcj02Towgs84(double lng, double lat) {
        double[] delta = calDelta(lng, lat);
        return new double[]{lng - delta[0], lat - delta[1]};
    }

    /**
     * 计算经纬度差
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 经纬度差数组, delta[0]经度差，delta[1]纬度差
     */
    private static double[] calDelta(double lng, double lat) {
        double[] delta = new double[2];
        double dLat = calDeltaLat(lng - 105.0, lat - 35.0);
        double dlng = calDeltaLng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * Math.PI;
        double magic = Math.sin(radlat);
        magic = 1 - FLATTENING * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        delta[0] = (dlng * 180.0) / (EARTH_RADIUS_IN_METER / sqrtmagic * Math.cos(radlat) * Math.PI);
        delta[1] = (dLat * 180.0) / ((EARTH_RADIUS_IN_METER * (1 - FLATTENING)) / (magic * sqrtmagic) * Math.PI);
        return delta;
    }

    /**
     * 纬度转换
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 返回纬度差
     */
    private static double calDeltaLat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * Math.PI) + 320 * Math.sin(lat * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 经度转换
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 返回经度差
     */
    private static double calDeltaLng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * Math.PI) + 40.0 * Math.sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * Math.PI) + 300.0 * Math.sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }
}

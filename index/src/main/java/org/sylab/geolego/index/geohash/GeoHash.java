package org.sylab.geolego.index.geohash;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.*;

/**
 * @author : suiyuan
 * @description :
 * @date : Created in 2019-05-21 09:28
 * @modified by :
 **/
public class GeoHash {

    private static final double MAX_LNG = 180.0;
    private static final double MIN_LNG = -180.0;
    private static final double MAX_LAT = 90.0;
    private static final double MIN_LAT = -90.0;

    private Point point;

    private int hashLength = 8;
    private int lngLength = 20;
    private int latLength = 20;

    private double lngResolution;
    private double latResolution;

    private String code;

    private Polygon extent;

    /**
     * base32字符集合
     */
    private static final char[] BASE32_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private Map<Character, Integer> base32CharMap;

    public GeoHash() {

    }

    public GeoHash(int hashLength) {
        this.hashLength = hashLength;
        initParams();
        initChars();
    }

    public GeoHash(String hashCode) {
        this.hashLength = hashCode.length();
        this.code = hashCode;
        initParams();
        initChars();
        this.extent = toPolygon(getGeoPolygon(), 4326);
    }

    public List<String> getGeoHashCode9(double lng, double lat) {
        return null;
    }


    /**
     * 获取指定经纬度所属geohash编码
     *
     * @param lng
     * @param lat
     * @return
     */
    public String getGeoHashCode(double lng, double lat) {
        boolean[] binaryCode = getGeoCode(lng, lat);
        if (binaryCode == null || binaryCode.length == 0) {
            return "";
        }
        //5位一个编码，循环
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < binaryCode.length; i += 5) {
            boolean[] base32 = new boolean[5];
            for (int j = 0; j < 5; j++) {
                base32[j] = binaryCode[i + j];
            }
            char c = getBase32Char(base32);
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    /**
     * 计算周围顺时针8个geohash（包括自己，自己为第一个）
     *
     * @param lng
     * @param lat
     * @return
     */
    public String[] getAround(double lng, double lat) {
        //基于坐标分别计算周围8个geohash
        String[] arounds = new String[9];

        double lngLeft = lng - lngResolution;
        double lngRight = lng + lngResolution;
        double latUp = lat + latResolution;
        double latDown = lat - latResolution;
        //本身
        arounds[0] = getGeoHashCode(lng, lat);
        //左上
        arounds[1] = getGeoHashCode(lngLeft, latUp);
        //中上
        arounds[2] = getGeoHashCode(lng, latUp);
        //右上
        arounds[3] = getGeoHashCode(lngRight, latUp);
        //右中
        arounds[4] = getGeoHashCode(lngRight, lat);
        //右下
        arounds[5] = getGeoHashCode(lngRight, latDown);
        //中下
        arounds[6] = getGeoHashCode(lng, latDown);
        //左下
        arounds[7] = getGeoHashCode(lngLeft, latDown);
        //左中
        arounds[8] = getGeoHashCode(lngLeft, lat);
        return arounds;
    }

    /**
     * 根据geohash编码获取对应polygon
     *
     * @param geohashCode
     * @return
     */
    public Envelope getGeoPolygon(String geohashCode) {
        boolean[] binaryCode = new boolean[hashLength * 5];
        int i = 0;
        for (char c : geohashCode.toCharArray()) {
            boolean[] base32 = getBase32Code(c);
            for (int j = 0; j < 5; j++) {
                binaryCode[i++] = base32[j];
            }
        }

        //get coordinate range
        return getGeoEnvelope(binaryCode);
    }

    /**
     * 获取当前geohash的范围
     *
     * @return
     */
    public Envelope getGeoPolygon() {
        return this.extent.getEnvelopeInternal();
    }

    /**
     * 获取下一级别所有geohash对象
     */
    public List<GeoHash> getSubGeoHashLst() {
        if (this.hashLength == 12) {
            throw new RuntimeException("code can not shorter！");
        }

        List<GeoHash> subGeohashList = new ArrayList<>();
        for (char base32Char : BASE32_CHARS) {
            String subCode = this.getCode() + base32Char;
            subGeohashList.add(new GeoHash(subCode));
        }
        return subGeohashList;
    }

    private void initChars() {
        base32CharMap = new HashMap<>(BASE32_CHARS.length);
        for (int i = 0; i < BASE32_CHARS.length; i++) {
            base32CharMap.put(BASE32_CHARS[i], i);
        }
    }

    /**
     * 初始化参数
     */
    private void initParams() {
        if (hashLength < 0 || hashLength > 12) {
            return;
        }

        //初始化经纬度编码长度
        latLength = hashLength * 5 / 2;
        if (hashLength % 2 == 0) {
            lngLength = latLength;
        } else {
            lngLength = latLength + 1;
        }

        //初始化经纬度编码分辨率
        lngResolution = MAX_LNG - MIN_LNG;
        for (int i = 0; i < lngLength; i++) {
            lngResolution /= 2.0;
        }
        latResolution = MAX_LAT - MIN_LAT;
        for (int i = 0; i < latLength; i++) {
            latResolution /= 2.0;
        }
    }

    /**
     * 根据二进制数组获取对应的base32字符
     *
     * @param base32
     * @return
     */
    private char getBase32Char(boolean[] base32) {
        if (base32 == null || base32.length != 5) {
            return ' ';
        }
        int num = 0;
        for (boolean bool : base32) {
            num <<= 1;
            if (bool) {
                num += 1;
            }
        }
        return BASE32_CHARS[num % BASE32_CHARS.length];
    }

    /**
     * 根据base32字符获取二进制数组
     *
     * @param c
     * @return
     */
    private boolean[] getBase32Code(char c) {
        int index = base32CharMap.get(c);
        boolean[] base32 = new boolean[5];
        int j = 0;
        for (int i = 4; i >= 0; i--) {
            boolean bool = false;
            if ((index >>> i & 1) == 1) {
                bool = true;
            }
            base32[j++] = bool;
        }
        return base32;
    }


    private Envelope getGeoEnvelope(boolean[] binaryCode) {

        boolean[] lngCode = new boolean[lngLength];
        boolean[] latCode = new boolean[latLength];
        int lngIndex = 0;
        int latIndex = 0;
        for (int i = 0; i < binaryCode.length; i++) {
            if (i % 2 == 0) {
                lngCode[lngIndex++] = binaryCode[i];
            } else {
                latCode[latIndex++] = binaryCode[i];
            }
        }
        double[] lngRange = getGeoRange(lngCode, MIN_LNG, MAX_LNG);
        double[] latRange = getGeoRange(latCode, MIN_LAT, MAX_LAT);

        return new Envelope(lngRange[0], lngRange[1], latRange[0], latRange[1]);
    }

    private double[] getGeoRange(boolean[] code, double min, double max) {
        double[] range = new double[2];
        for (int i = 0; i < code.length; i++) {
            double mid = (min + max) / 2;
            if (code[i]) {
                min = mid;
            } else {
                max = mid;
            }
        }
        range[0] = min;
        range[1] = max;

        return range;
    }

    /**
     * 获取geohash二进制编码
     *
     * @param lng
     * @param lat
     * @return
     */
    private boolean[] getGeoCode(double lng, double lat) {
        boolean[] lngCodes = getHashCode(lng, MIN_LNG, MAX_LNG, lngLength);
        boolean[] latCodes = getHashCode(lat, MIN_LAT, MAX_LAT, latLength);
        return mergeCode(lngCodes, latCodes);
    }

    /**
     * 获取某一维度的二进制编码
     *
     * @param value
     * @param min
     * @param max
     * @param length
     * @return
     */
    private boolean[] getHashCode(double value, double min, double max, int length) {
        if (value < min || value > max || length == 0) {
            return null;
        }
        boolean[] codes = new boolean[length];
        for (int i = 0; i < length; i++) {
            double mid = (max + min) / 2;
            if (value > mid) {
                codes[i] = true;
                min = mid;
            } else {
                codes[i] = false;
                max = mid;
            }
        }
        return codes;
    }

    /**
     * 二进制编码合并
     *
     * @param lngCodes
     * @param latCodes
     * @return
     */
    private boolean[] mergeCode(boolean[] lngCodes, boolean[] latCodes) {
        if (lngCodes == null || lngCodes.length == 0) {
            return null;
        }
        if (latCodes == null || latCodes.length == 0) {
            return null;
        }
        boolean[] finalCodes = new boolean[lngCodes.length + latCodes.length];
        for (int i = 0; i < lngCodes.length; i++) {
            finalCodes[2 * i] = lngCodes[i];
        }
        for (int i = 0; i < latCodes.length; i++) {
            finalCodes[2 * i + 1] = latCodes[i];
        }
        return finalCodes;
    }

    /**
     * 获取env的plg
     *
     * @param envelope
     * @param srid
     * @return
     */
    public static Polygon toPolygon(Envelope envelope, int srid) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), srid);
        CoordinateSequence cs = new CoordinateArraySequence(new Coordinate[]{new Coordinate(envelope.getMinX(), envelope.getMaxY()), new Coordinate(envelope.getMaxX(), envelope.getMaxY()), new Coordinate(envelope.getMaxX(), envelope.getMinY()), new Coordinate(envelope.getMinX(), envelope.getMinY()), new Coordinate(envelope.getMinX(), envelope.getMaxY())});
        LinearRing shell = new LinearRing(cs, geometryFactory);
        return geometryFactory.createPolygon(shell);
    }

    public String getCode() {
        return code;
    }

    public Polygon getExtent() {
        return extent;
    }
}

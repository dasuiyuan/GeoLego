package org.sylab.geolego.index.model;

import java.util.Map;

/**
 * @author : suiyuan
 * @description : 查询参数类
 * @date : Created in 2019-10-11 16:00
 * @modified by :
 **/
public class QueryParams {
    /**
     * 空间字段
     */
    private String geomField;
    /**
     * 时间字段
     */
    private String timeField;

    /**
     * 经度值
     */
    private double lng;
    /**
     * 纬度值
     */
    private double lat;

    /**
     * 空间范围
     */
    private String bbox;

    /**
     * 起始日期
     */
    private String startTime;
    /**
     * 终止日期
     */
    private String endTime;

    private Map<String, String> attrMap;

    private QueryParams(QueryParamsBuilder builder) {
        this.geomField = builder.geomField;
        this.timeField = builder.timeField;
        this.lng = builder.lng;
        this.lat = builder.lat;
        this.bbox = builder.bbox;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.attrMap = builder.attrMap;
    }

    /**
     * 几何字段
     */
    public String getGeomField() {
        return geomField;
    }

    /**
     * 时间字段
     */
    public String getTimeField() {
        return timeField;
    }

    /**
     * 经度值
     */
    public double getLng() {
        return lng;
    }

    /**
     * 纬度值
     */
    public double getLat() {
        return lat;
    }

    /**
     * 起始日期
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * 终止日期
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * 属性过滤Map
     */
    public Map<String, String> getAttrMap() {
        return attrMap;
    }

    /**
     * 范围bbox
     */
    public String getBbox() {
        return bbox;
    }

    public static class QueryParamsBuilder {
        /**
         * 空间字段
         */
        private String geomField;
        /**
         * 时间字段
         */
        private String timeField;

        /**
         * 经度
         */
        private double lng;
        /**
         * 纬度
         */
        private double lat;

        /**
         * 空间范围
         */
        private String bbox;

        /**
         * 起始时间
         */
        private String startTime;
        /**
         * 中止时间
         */
        private String endTime;

        /**
         * 属性过滤Map
         */
        private Map<String, String> attrMap;

        public QueryParamsBuilder(String geomField, String timeField, double lng, double lat, String startTime, String endTime) {
            this.setGeomField(geomField);
            this.setTimeField(timeField);
            this.setLng(lng);
            this.setLat(lat);
            this.setStartTime(startTime);
            this.setEndTime(endTime);
        }

        public QueryParamsBuilder(String timeField, String startTime, String endTime) {
            this.setTimeField(timeField);
            this.setStartTime(startTime);
            this.setEndTime(endTime);
        }

        public QueryParamsBuilder attrMap(Map<String, String> attrMap) {
            this.attrMap = attrMap;
            return this;
        }

        public QueryParamsBuilder setGeomField(String geomField) {
            this.geomField = geomField;
            return this;
        }

        public QueryParamsBuilder setTimeField(String timeField) {
            this.timeField = timeField;
            return this;
        }

        public QueryParamsBuilder setLng(double lng) {
            this.lng = lng;
            return this;
        }

        public QueryParamsBuilder setLat(double lat) {
            this.lat = lat;
            return this;
        }

        public QueryParamsBuilder setBbox(String bbox) {
            this.bbox = bbox;
            return this;
        }

        public QueryParamsBuilder setStartTime(String startTime) {
            this.startTime = startTime;
            return this;
        }

        public QueryParamsBuilder setEndTime(String endTime) {
            this.endTime = endTime;
            return this;
        }

        public QueryParams build() {
            return new QueryParams(this);
        }
    }
}

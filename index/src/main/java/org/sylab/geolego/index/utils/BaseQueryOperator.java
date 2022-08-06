package org.sylab.geolego.index.utils;

import lombok.extern.slf4j.Slf4j;
import org.geotools.filter.text.cql2.CQLException;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.index.model.QueryParams;

import java.io.IOException;
import java.util.List;

/**
 * @author : suiyuan
 * @description : 业务查询抽象类
 * @date : Created in 2019-10-11 14:54
 * @modified by :
 **/
@Slf4j
public abstract class BaseQueryOperator {
    /**
     * 缓存距离
     */
    private double offsetDis = 250;

    /**
     * 空间参考
     */
    private static final int SRID = 4326;
    /**
     * 地球长半径
     */
    private static final double EARTH_RADIUS_IN_METER = 6378137.0;

    /**
     * 设置缓存距离
     *
     * @param offsetDis 缓存距离
     */
    public void setOffsetDis(double offsetDis) {
        this.offsetDis = offsetDis;
    }

    /**
     * 时间 + 多属性查询
     *
     * @param queryParams 查询参数
     * @return 查询结果
     * @throws IOException  IOException
     * @throws CQLException CQLException
     */
    public List<SimpleFeature> queryTemporalWithAttrMap(QueryParams queryParams) throws IOException, CQLException {
        String cql = String.format("%s DURING %sT00:00:00Z/%sT00:00:00Z", queryParams.getTimeField(), queryParams.getStartTime(), queryParams.getEndTime());
        if (queryParams.getAttrMap() != null && queryParams.getAttrMap().size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            queryParams.getAttrMap().forEach((key, value) -> stringBuilder.append(String.format(" AND %s = '%s'", key, value)));
            cql += stringBuilder.toString();
        }
        log.info("当前 cql：" + cql);
        return GeomesaQueryEngine.QueryFeatures(getSftName(), cql);
    }

    /**
     * 时空查询
     *
     * @param queryParams 查询参数
     * @return 查询结果
     */
    public List<SimpleFeature> querySpatialTemporal(QueryParams queryParams) throws IOException, CQLException {
        String cql = getBaseCQL(queryParams);
        log.info("当前offsetDis：" + offsetDis + ", cql：" + cql);
        return GeomesaQueryEngine.QueryFeatures(getSftName(), cql);
    }

    /**
     * 时空查询
     *
     * @param queryParams 查询参数
     * @param dis         偏移范围
     * @return 查询结果
     * @throws IOException  IOException
     * @throws CQLException CQLException
     */
    public List<SimpleFeature> querySpatialTemporalDistance(QueryParams queryParams, double dis) throws IOException, CQLException {
        Point point = new Point(new CoordinateArraySequence(new Coordinate[]{new Coordinate(queryParams.getLng(), queryParams.getLat(), 0.0)}), new GeometryFactory(new PrecisionModel(), SRID));
        Envelope envelope = getGeoFenceRough(point, dis);
        String cql = String.format("BBOX(%s,%s,%s,%s,%s) AND %s DURING %sT00:00:00Z/%sT00:00:00Z",
                queryParams.getGeomField(), envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                queryParams.getTimeField(), queryParams.getStartTime(), queryParams.getEndTime());
        log.info("当前offsetDis：" + dis + ", cql：" + cql);
        return GeomesaQueryEngine.QueryFeatures(getSftName(), cql);
    }

    /**
     * 时空+多属性查询
     *
     * @return 查询结果
     */
    public List<SimpleFeature> querySpatialTemporalWithAttrMap(QueryParams queryParams) throws IOException, CQLException {
        String cql = getBaseCQL(queryParams);
        if (queryParams.getAttrMap() != null && queryParams.getAttrMap().size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            queryParams.getAttrMap().forEach((key, value) -> stringBuilder.append(String.format(" AND %s = '%s'", key, value)));
            cql += stringBuilder.toString();
        }
        log.info("当前offsetDis：" + offsetDis + ", cql：" + cql);
        return GeomesaQueryEngine.QueryFeatures(getSftName(), cql);
    }

    /**
     * 获取基本时空过滤语句
     *
     * @param queryParams 查询参数
     * @return 时空cql
     */
    private String getBaseCQL(QueryParams queryParams) {
        String cql;
        if (queryParams.getBbox() != null) {
            cql = String.format("BBOX(%s,%s) AND %s DURING %sT00:00:00Z/%sT00:00:00Z",
                    queryParams.getGeomField(), queryParams.getBbox(),
                    queryParams.getTimeField(), queryParams.getStartTime(), queryParams.getEndTime());
        } else {
            Point point = new Point(new CoordinateArraySequence(new Coordinate[]{new Coordinate(queryParams.getLng(), queryParams.getLat(), 0.0)}), new GeometryFactory(new PrecisionModel(), SRID));
            Envelope envelope = getGeoFenceRough(point, offsetDis);
            cql = String.format("BBOX(%s,%s,%s,%s,%s) AND %s DURING %sT00:00:00Z/%sT00:00:00Z",
                    queryParams.getGeomField(), envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                    queryParams.getTimeField(), queryParams.getStartTime(), queryParams.getEndTime());
        }
        return cql;
    }

    /**
     * 获取sft名
     *
     * @return sft名
     */
    protected abstract String getSftName();

    /**
     * 获取地理围栏
     *
     * @param centPt   目标点
     * @param distance 单位M
     * @return 围栏范围
     */
    private Envelope getGeoFence(Point centPt, double distance) {
        Envelope fence;
        double perimeter = 2 * Math.PI * EARTH_RADIUS_IN_METER;
        double latPerM = 360 / perimeter;
        double lngPerM = 360 / (perimeter * Math.cos(centPt.getY()));
        double latBuffLen = distance * latPerM;
        double lngBuffLen = distance * lngPerM;
        fence = new Envelope(centPt.getX() - lngBuffLen, centPt.getX() + lngBuffLen, centPt.getY() - latBuffLen, centPt.getY() + latBuffLen);
        return fence;
    }

    /**
     * 地理范围粗略距离
     *
     * @param centPt   中心点
     * @param distance 缓冲范围
     * @return 围栏范围
     */
    private Envelope getGeoFenceRough(Point centPt, double distance) {
        double degree = distance / 100000;
        return new Envelope(centPt.getX() - degree, centPt.getX() + degree, centPt.getY() - degree, centPt.getY() + degree);
    }
}

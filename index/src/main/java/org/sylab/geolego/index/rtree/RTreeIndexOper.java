package org.sylab.geolego.index.rtree;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.GeometryItemDistance;
import org.locationtech.jts.index.strtree.STRtree;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : suiyuan
 * @description : R树索引辅助类
 * @date : Created in 2019-04-19 11:31
 * @modified by :
 **/
public class RTreeIndexOper implements Serializable {
    /**
     * rtree索引对象
     */
    private STRtree strtree;

    /**
     * R树索引辅助类
     *
     * @param geometries 几何对象集合
     */
    public RTreeIndexOper(List<Geometry> geometries) {
        strtree = new STRtree();
        geometries.forEach(g -> {
            Envelope bbox = g.getEnvelopeInternal();
            strtree.insert(bbox, g);
        });
    }

    /**
     * R树索引辅助类
     */
    public RTreeIndexOper() {
        strtree = new STRtree();
    }

    /**
     * 添加geometry
     *
     * @param geom 几何对象
     */
    public void add(Geometry geom) {
        if (geom == null) {
            return;
        }
        Envelope bbox = geom.getEnvelopeInternal();
        strtree.insert(bbox, geom);
    }

    /**
     * 建立索引
     */
    public void buildIndex() {
        strtree.build();
    }

    /**
     * 查询相交对象
     *
     * @param g 查询对象
     * @return 相交的结合对象集合
     */
    public List<Geometry> searchIntersect(Geometry g, boolean parallel) {
        if (g == null) {
            return null;
        }
        Envelope bbox = g.getEnvelopeInternal();
        List<Geometry> candidates = strtree.query(bbox);
        if (parallel) {
            return candidates.parallelStream().filter(o -> !g.equals(o) && g.intersects(o)).collect(Collectors.toList());
        } else {
            return candidates.stream().filter(o -> !g.equals(o) && g.intersects(o)).collect(Collectors.toList());
        }
    }

    /**
     * 查询相交对象
     *
     * @param g 查询对象
     * @return 相交的结合对象集合
     */
    public List<Geometry> searchIntersectEstmate(Geometry g) {
        if (g == null) {
            return null;
        }
        Envelope bbox = g.getEnvelopeInternal();
        List<Geometry> candidates = strtree.query(bbox);
        return candidates;
    }

    /**
     * 在指定范围内寻找集合对象最近的k个元素
     *
     * @param env
     * @param g
     * @param k
     * @return
     */
    public List<Geometry> knn(Envelope env, Geometry g, int k) {
        Object[] result = strtree.nearestNeighbour(env, g, new GeometryItemDistance(), k);
        if (result == null) return null;
        return Arrays.stream(result).map(o -> (Geometry) o).collect(Collectors.toList());
    }
}

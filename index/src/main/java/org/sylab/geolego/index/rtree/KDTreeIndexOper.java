package org.sylab.geolego.index.rtree;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : suiyuan
 * @description : KD树索引辅助类
 * @date : Created in 2019-04-19 11:31
 * @modified by :
 **/
public class KDTreeIndexOper<T extends Point> {

    /**
     * kdTree对象
     */
    private KdTree kdTree;

    /**
     * KD树索引辅助类
     *
     * @param points 点数据
     */
    public KDTreeIndexOper(List<T> points) {
        kdTree = new KdTree();
        for (Point point : points) {
            kdTree.insert(point.getCoordinate(), point);
        }
    }

    public KDTreeIndexOper() {
        kdTree = new KdTree();
    }

    /**
     * 插入点数据
     *
     * @param point 点
     */
    public void insertPoint(T point) {
        kdTree.insert(point.getCoordinate(), point);
    }

    /**
     * 获取在地理围栏内的对象
     *
     * @param envelope 范围
     * @return 结果坐标集
     */
    public List<T> queryInclude(Envelope envelope) {

        List nodes = kdTree.query(envelope);
        if (nodes == null || nodes.size() == 0) {
            return null;
        }
        List<T> resultPoints;
        List<T> candidatePoints = new ArrayList<>(nodes.size());
        for (Object o : nodes) {
            candidatePoints.add((T) (((KdNode) o).getData()));
        }
        return candidatePoints;
    }

    /**
     * 获取在地理围栏内的对象数量
     *
     * @param envelope 范围
     * @return 结果坐标集
     */
    public long queryCount(Envelope envelope) {
        List<KdNode> nodes = kdTree.query(envelope);
        if (nodes == null || nodes.size() == 0) {
            return 0;
        }
        long sum = nodes.stream().collect(Collectors.summarizingInt(KdNode::getCount)).getSum();
        return sum;
    }
}

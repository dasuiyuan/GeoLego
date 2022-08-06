package org.sylab.geolego.index.quadtree;

import lombok.extern.slf4j.Slf4j;
import org.geotools.filter.text.cql2.CQLException;
import org.sylab.geolego.index.utils.BaseQueryOperator;

import java.io.IOException;

/**
 * @author : suiyuan
 * @description : 线性四叉树查询权重索引
 * @date : Created in 2019-10-21 13:30
 * @modified by :
 **/
@Slf4j
public class QTreeQueryWeightIndexLinear extends QTreeQueryWeightIndex {
    public QTreeQueryWeightIndexLinear(BaseQueryOperator queryOperator, String geomField, String timeField, String startTime, String endTime, QTreeBBox entirRange, double minLength, double maxLength, int resultThreshold) {
        super(queryOperator, geomField, timeField, startTime, endTime, entirRange, minLength, maxLength, resultThreshold);
    }



    /**
     * 分裂节点到最低级别
     *
     * @param node 节点
     * @throws IOException  IOException
     * @throws CQLException CQLException
     */
    @Override
    protected  void dividNode(QTreeNode node) throws IOException, CQLException {
        long count = queryNode(node);
        log.info("node: " + node.getBbox() + "count: " + count);
        //如果返回记录数小于阈值，或者，节点长或宽小于长度下限，则停止分裂
        if (count < resultThreshold || node.getWidth() * node.getHeight() < minLength * minLength) {
            log.info("area: " + node.getWidth() * node.getHeight() + "return！");
            //将距离传递到最底层
            splitNode2Bottom(node);
            return;
        }
        node.split();
        QTreeNode[] children = node.getChildren();
        for (int i = 0; i < 4; i++) {
            dividNode(children[i]);
        }
    }

    /**
     * 分裂节点到最底层
     *
     * @param node 节点
     */
    protected void splitNode2Bottom(QTreeNode node) {
        if (node.getLevel() == bottomLevel) {
            return;
        }
        node.split();
        QTreeNode[] children = node.getChildren();
        for (int i = 0; i < 4; i++) {
            children[i].setWeight(node.getWeight());
            splitNode2Bottom(children[i]);
        }
    }
}

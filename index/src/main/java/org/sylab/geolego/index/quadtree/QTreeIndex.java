package org.sylab.geolego.index.quadtree;


import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author : suiyuan
 * @description : 四叉树查询权重索引
 * @date : Created in 2019-10-19 17:08
 * @modified by :
 **/
@Slf4j
public class QTreeIndex implements Serializable {
    /**
     * 根节点
     */
    protected QTreeNode root;

    /**
     * 整体范围 ( 尽量保持长宽一致)
     */
    protected QTreeBBox entireRange;

    /**
     * 最低级别节点列表
     */
    protected List<QTreeNode> endLevelNodeList;

    /**
     * 级别
     */
    protected int level;

    public QTreeIndex(QTreeBBox entireRange, int level) {
        this.entireRange = entireRange;
        this.level = level;
        endLevelNodeList = new LinkedList<>();
    }


    /**
     * 生成一棵到上边界的满四叉树
     */
    public void initTree() {
        root = new QTreeNode(null, entireRange, 0, "0");
        buildQTree(root);
    }


    /**
     * 递归生成树
     *
     * @param node 节点
     */
    protected void buildQTree(QTreeNode node) {
        //如果节点面积抵达上限，则加入起始节点列表并退出
        if (node.getLevel() == level) {
            log.info(String.format("到达起始级别:%s width:%f height:%f", node.getLevel(), node.getWidth(), node.getHeight()));
            endLevelNodeList.add(node);
            return;
        }
        node.split();
        log.info(String.format("第%s级分裂", node.getLevel()));
        QTreeNode[] children = node.getChildren();
        for (int i = 0; i < 4; i++) {
            buildQTree(children[i]);
        }
    }

    /**
     * 找到坐标对应的节点
     *
     * @param lng  经度
     * @param lat  纬度
     * @param node 节点
     * @return 目标节点
     */
    public QTreeNode queryNode(double lng, double lat, QTreeNode node) {
        if (node == null) {
            return null;
        }
        if (node.hasChild()) {
            QTreeNode[] children = node.getChildren();
            for (QTreeNode child : children) {
                if (child.getBbox().contains(lng, lat)) {
                    if (child.hasChild()) {
                        return queryNode(lng, lat, child);
                    } else {
                        return child;
                    }
                }
            }
        }
        return node;
    }

    /**
     * 获取指定code的node的索引
     *
     * @param code code
     * @return node索引
     */
    public int getNodeIndex(String code) {
        if (endLevelNodeList != null) {
            for (int i = 0; i < endLevelNodeList.size(); i++) {
                if (endLevelNodeList.get(i).getCode().equals(code)) {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * 根据node序号获取node
     *
     * @param index node索引
     * @return node
     */
    public QTreeNode getNodeByIndex(int index) {
        if (endLevelNodeList != null) {
            return endLevelNodeList.get(index);
        }
        return null;
    }

    /**
     * 根节点
     */
    public QTreeNode getRoot() {
        return root;
    }

    public void output() {
        Queue<QTreeNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            QTreeNode node = queue.poll();
            if (node.hasChild()) {
                for (int i = 0; i < 4; i++) {
                    queue.offer(node.getChildren()[i]);
                }
            } else {
                String feature = node.getLevel() + "&" + node.getCode() + "&" + node.getWidth() + "&" + node.getHeight();
                System.out.println(feature);
            }
        }
    }
}

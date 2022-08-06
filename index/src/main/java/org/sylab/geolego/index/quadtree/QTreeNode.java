package org.sylab.geolego.index.quadtree;

import java.io.Serializable;

/**
 * @author : suiyuan
 * @description : 四叉树节点
 * @date : Created in 2019-10-19 17:06
 * @modified by :
 **/
public class QTreeNode implements Serializable {
    /**
     * 空间范围
     */
    private QTreeBBox bbox;

    /**
     * 级别
     */
    private int level;

    /**
     * 编码
     * *
     * 0  *   3
     * *  *  *  *  *
     * 1  *   2
     * *
     */
    private String code;

    /**
     * 权重
     */
    private double weight;

    /**
     * 自定义数据
     */
    private Object userData;

    /**
     * 子节点
     */
    private QTreeNode[] children;

    /**
     * 父节点
     */
    private QTreeNode parent;


    public QTreeNode(QTreeNode parent, QTreeBBox bbox, int level, String code) {
        this.parent = parent;
        this.bbox = bbox;
        this.level = level;
        this.code = code;
    }

    public QTreeBBox getBbox() {
        return bbox;
    }

    public void setBbox(QTreeBBox bbox) {
        this.bbox = bbox;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Object userData) {
        this.userData = userData;
    }

    public double getWidth() {
        return (bbox.getMaxLng() - bbox.getMinLng());
    }

    public double getHeight() {
        return (bbox.getMaxLat() - bbox.getMinLat());
    }

    public double getMidLng() {
        return (bbox.getMaxLng() + bbox.getMinLng()) / 2;
    }

    public double getMidLat() {
        return (bbox.getMaxLat() + bbox.getMinLat()) / 2;
    }

    public QTreeNode[] getChildren() {
        return children;
    }

    public void setChildren(QTreeNode[] children) {
        this.children = children;
    }

    public boolean hasChild() {
        return children != null;
    }

    /**
     * 节点分裂
     */
    public void split() {
        QTreeBBox[] siblingbbxes = bbox.split();
        children = new QTreeNode[4];
        children[0] = new QTreeNode(this, siblingbbxes[0], this.getLevel() + 1, this.getCode() + "0");
        children[1] = new QTreeNode(this, siblingbbxes[1], this.getLevel() + 1, this.getCode() + "1");
        children[2] = new QTreeNode(this, siblingbbxes[2], this.getLevel() + 1, this.getCode() + "2");
        children[3] = new QTreeNode(this, siblingbbxes[3], this.getLevel() + 1, this.getCode() + "3");
    }

    /**
     * 获取坐标所在当前节点的编码
     */
    public int getCode(double lng, double lat) {
        if (bbox.getMinLng() < lng && lng < (bbox.getMinLng() + bbox.getMaxLng()) / 2 && (bbox.getMaxLat() + bbox.getMinLat()) / 2 < lat && lat < bbox.getMaxLat()) {
            return 0;
        } else if (bbox.getMinLng() < lng && lng < (bbox.getMaxLng() + bbox.getMinLng()) / 2 && bbox.getMinLat() < lat && lat < (bbox.getMaxLat() + bbox.getMinLat()) / 2) {
            return 1;
        } else if ((bbox.getMaxLng() + bbox.getMinLng()) / 2 < lng && lng < bbox.getMaxLng() && bbox.getMinLat() < lat && lat < (bbox.getMaxLat() + bbox.getMinLat()) / 2) {
            return 2;
        } else if ((bbox.getMaxLng() + bbox.getMinLng()) / 2 < lng && lng < bbox.getMaxLng() && (bbox.getMaxLat() + bbox.getMinLat()) / 2 < lat && lat < bbox.getMaxLat()) {
            return 3;
        }
        return 0;
    }

    /**
     * 层级
     */
    public int getLevel() {
        return level;
    }

    /**
     * 编码
     * *
     * 0  *   3
     * *  *  *  *  *
     * 1  *   2
     * *
     */
    public String getCode() {
        return code;
    }

    /**
     * 父节点
     */
    public QTreeNode getParent() {
        return parent;
    }
}

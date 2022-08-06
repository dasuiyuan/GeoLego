package org.sylab.geolego.index.quadtree;


import org.locationtech.jts.geom.Envelope;

import java.io.Serializable;

/**
 * @author : suiyuan
 * @description : 四叉树范围对象
 * @date : Created in 2019-10-19 17:07
 * @modified by :
 **/
public class QTreeBBox implements Serializable {
    /**
     * 最小经度
     */
    private double maxLng;
    /**
     * 最大经度
     */
    private double minLng;
    /**
     * 最小纬度
     */
    private double maxLat;
    /**
     * 最大纬度
     */
    private double minLat;

    public double getMaxLng() {
        return maxLng;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public double getMinLng() {
        return minLng;
    }

    public double getMinLat() {
        return minLat;
    }

    public QTreeBBox(double minx, double maxx, double miny, double maxy) {
        this.maxLng = maxx;
        this.minLng = minx;
        this.maxLat = maxy;
        this.minLat = miny;
    }

    /**
     * 分裂
     *
     * @return 分裂后4个子bbox
     */
    public QTreeBBox[] split() {

        QTreeBBox[] bbox = new QTreeBBox[4];
        double midx = (this.minLng + this.maxLng) / 2;
        double midy = (this.minLat + this.maxLat) / 2;
        // ul,ll,lr,ur
        bbox[0] = new QTreeBBox(minLng, midx, midy, maxLat);
        bbox[1] = new QTreeBBox(minLng, midx, minLat, midy);
        bbox[2] = new QTreeBBox(midx, maxLng, minLat, midy);
        bbox[3] = new QTreeBBox(midx, maxLng, midy, maxLat);
        return bbox;
    }

    /**
     * 是否包含对应点
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 结果
     */
    public boolean contains(double lng, double lat) {
        return lng > minLng
                && lng < maxLng
                && lat > minLat
                && lat < maxLat;
    }


    /**
     * 获取bbox范围（minLng ,minLat ,maxLng ,maxLat）
     *
     * @return bbox范围
     */
    public String getBBox() {
        return String.format("%.5f,%.5f,%.5f,%.5f", minLng, minLat, maxLng, maxLat);
    }

    /**
     * 获取Envelope对象
     *
     * @return Envelope对象
     */
    public Envelope getEnv() {
        return new Envelope(minLng, maxLng, minLat, maxLat);
    }

    @Override
    public String toString() {
        return String.format("POLYGON ((" + minLng + " " + maxLat + ", " + minLng + " " + minLat + "," + maxLng + " " + minLat + "," + maxLng + " " + maxLat + "," + minLng + " " + maxLat + "))");
    }
}

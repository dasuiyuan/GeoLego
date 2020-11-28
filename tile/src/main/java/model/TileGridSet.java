package model;

import lombok.Data;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author : suiyuan
 * @description : 瓦片网格定义类
 * @date : Created in 2020-05-12 09:38
 * @modified by :
 **/
@Data
public class TileGridSet {

    /**
     * 瓦片集名称
     */
    private String name;

    /**
     *  空间参考
     */
    private CoordinateReferenceSystem crs;

    /**
     *  分辨率金字塔
     */
    private double[] matrixSet;

    /**
     *  地理范围
     */
    private Envelope bounds;

    /**
     *  瓦片宽度
     */
    private int tileWidth;

    /**
     *  瓦片高度
     */
    private int tileHeight;

    /**
     *  起算点
     */
    private Point origin;

}

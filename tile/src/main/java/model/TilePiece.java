package model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

/**
 * @author : suiyuan
 * @description : 瓦片块
 * @date : Created in 2020-04-24 10:25
 * @modified by :
 **/
@Data
@Accessors(chain = true)
public class TilePiece {

    /**
     * 所属级别
     */
    private int level;

    /**
     * 所属列号
     */
    private int colNum;

    /**
     * 所属行号
     */
    private int rowNum;

    /**
     * 地理范围
     */
    private Envelope mbr;

    /**
     * 瓦片内的集合对象集合
     */
    private List<Geometry> geometries;

    public TilePiece(int level, int colNum, int rowNum) {
        this.level = level;
        this.colNum = colNum;
        this.rowNum = rowNum;
    }

    @Override
    public String toString() {
        return String.format("%s_%s_%s", level + colNum + rowNum);
    }
}

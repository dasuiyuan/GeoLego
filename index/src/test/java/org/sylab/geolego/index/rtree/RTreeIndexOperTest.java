package org.sylab.geolego.index.rtree;

import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.sylab.geolego.index.utils.GeometryUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/5/4 0:12
 * @since:
 **/
public class RTreeIndexOperTest {

    @Test
    public void knn() throws ParseException {
        Geometry p1 = GeometryUtils.WKT_READER_2.read("POINT (116.389241958176 39.9190029153366)");
        Geometry p2 = GeometryUtils.WKT_READER_2.read("POINT (116.382884084233 39.9162035658993)");
        Geometry p3 = GeometryUtils.WKT_READER_2.read("POINT (116.386063021205 39.9125913330274)");
        Geometry p4 = GeometryUtils.WKT_READER_2.read("POINT (116.398307815466 39.9129525648876)");
        Geometry p5 = GeometryUtils.WKT_READER_2.read("POINT (116.392420895148 39.914939306065)");
        List<Geometry> pList = new ArrayList<>();
        pList.add(p1);
        pList.add(p2);
        pList.add(p3);
        pList.add(p4);
        pList.add(p5);

        Geometry p = GeometryUtils.WKT_READER_2.read("POINT(116.389110 39.914162)");
        RTreeIndexOper rTreeIndexOper = new RTreeIndexOper(pList);
        rTreeIndexOper.buildIndex();
        List<Geometry> knn = rTreeIndexOper.knn(new Envelope(116.38487, 116.39149, 39.91266, 39.92034), p, 1);
        Assert.assertTrue(GeometryUtils.WKT_WRITER_2.write(knn.get(0)).equals("POINT (116.392420895148 39.914939306065)"));

    }
}
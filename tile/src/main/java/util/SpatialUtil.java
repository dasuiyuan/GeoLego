package util;

import org.geotools.geometry.jts.GeometryClipper;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * @author : suiyuan
 * @description :
 * @date : Created in 2020-05-08 18:18
 * @modified by :
 **/
public class SpatialUtil {
    /**
     * 获取env的plg
     *
     * @param envelope
     * @param srid
     * @return
     */
    public static Polygon toPolygon(Envelope envelope, int srid) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), srid);
        CoordinateSequence cs = new CoordinateArraySequence(new Coordinate[]{
                new Coordinate(envelope.getMinX(), envelope.getMaxY()),
                new Coordinate(envelope.getMaxX(), envelope.getMaxY()),
                new Coordinate(envelope.getMaxX(), envelope.getMinY()),
                new Coordinate(envelope.getMinX(), envelope.getMinY()),
                new Coordinate(envelope.getMinX(), envelope.getMaxY())
        });
        LinearRing shell = new LinearRing(cs, geometryFactory);
        return geometryFactory.createPolygon(shell);
    }

    public static Geometry clip(Geometry geom, Envelope clippingEnvelope) {
        GeometryClipper clipper = new GeometryClipper(clippingEnvelope);
        try {
            return clipper.clip(geom, true);
        } catch (Exception e) {
            return clipper.clip(geom, false); // use non-robust clipper
        }
    }
}

package org.sylab.geolego.index.geohash;

import org.locationtech.jts.geom.*;

/**
 * @author : suiyuan
 * @description :
 * @date : Created in 2019-05-21 09:33
 * @modified by :
 **/
public class GeoHashDriver {
    public static void main(String[] args) {

//        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
//        Point point = factory.createPoint(new Coordinate(1, 2));

        GeoHash g = new GeoHash();
        GeoHash geoHash = new GeoHash(7);
//        String code = geoHash.getGeoHashCode(116.248283, 40.222012);

        Envelope envelope = geoHash.getGeoPolygon("wx4dvwb");
        System.out.println(envelope);
    }
}

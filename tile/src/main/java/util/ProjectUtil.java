package util;


import org.locationtech.jts.geom.*;

/**
 * @author : suiyuan
 * @description :
 * @date : Created in 2020-04-23 11:56
 * @modified by :
 **/
public class ProjectUtil {
//    private static Geometry project(Geometry geometry) throws FactoryException, TransformException {
//
//        CoordinateReferenceSystem crs = CRS.decode("EPSG:3857");
//        MathTransform mt = CRS.findMathTransform(DefaultGeographicCRS.WGS84, crs);
//        Geometry proGeom = JTS.transform(geometry, mt);
//        return proGeom;
//    }

    public static Polygon project(Polygon plg, int extent, Envelope mbr, double lngDelta, double latDelta) {
        double xRatio = extent / lngDelta;
        double yRatio = extent / latDelta;

        GeometryFactory geometryFactory = plg.getFactory();
        LineString exteriorRing = plg.getExteriorRing();
        Coordinate[] exCoordinates = exteriorRing.getCoordinates();
        Coordinate[] prjExCoodinates = new Coordinate[exCoordinates.length];
        for (int i = 0; i < exCoordinates.length; i++) {
            double prjX = Math.floor((exCoordinates[i].x - mbr.getMinX()) * xRatio);
            double prjY = extent - Math.floor((exCoordinates[i].y - mbr.getMinY()) * yRatio);
            prjExCoodinates[i] = new Coordinate(prjX, prjY);
        }

//        LineString inerRing = plg.getInteriorRingN(0);
//        Coordinate[] inCoordinates = inerRing.getCoordinates();
//        Coordinate[] prjInCoordinates = new Coordinate[inCoordinates.length];
//        for (int i = 0; i < inCoordinates.length; i++) {
//            int prjX = (int) inCoordinates[i].x * xRatio;
//            int prjY = extent - (int) inCoordinates[i].y * yRatio;
//            prjInCoordinates[i] = new Coordinate(prjX, prjY);
//        }

        Polygon prjPlg = geometryFactory.createPolygon(geometryFactory.createLinearRing(prjExCoodinates), null);
        prjPlg.setUserData(plg.getUserData());
        prjPlg.setSRID(plg.getSRID());
        return prjPlg;
    }
}

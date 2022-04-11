package org.sylab.geolego.model.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.DistanceOp;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/4/7 20:35
 * @since:
 **/
public class SpatialProjectUtils {
    private static final GeometryFactory FACTORY = JTSFactoryFinder.getGeometryFactory();

    @Data
    @AllArgsConstructor
    public static class ProjectPoint {
        private double distance;
        private double offsetLength;
        private Point raw;
    }

    public static ProjectPoint project(LineString line, Point pt) {
        DistanceOp distanceOp = new DistanceOp(line, pt);
        Coordinate[] coordinates = distanceOp.nearestPoints();
        if (coordinates != null) {
            double distanceInM = GeoFunction.getDistanceInM(coordinates[0], pt.getCoordinate());
            int segmentIndex = distanceOp.nearestLocations()[0].getSegmentIndex();
            double offsetDistanceInM = offsetLengthFromStartInM(line, segmentIndex) +
                    GeoFunction.getDistanceInM(line.getCoordinateN(segmentIndex), coordinates[0]);
            return new ProjectPoint(distanceInM, offsetDistanceInM, FACTORY.createPoint(coordinates[0]));
        }
        return null;
    }

    private static double offsetLengthFromStartInM(LineString lineString, int index) {
        if (index == 0) {
            return 0.0;
        }
        double distance = 0.0;
        for (int i = 0; i < index; i++) {
            distance += GeoFunction.getDistanceInM(lineString.getCoordinateN(i), lineString.getCoordinateN(i + 1));
        }
        return distance;
    }
}

package trajectory;

import lombok.Data;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.Assert;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/4/7 18:24
 * @since:
 **/
@Data
public class Trajectory {
    private static final GeometryFactory FACTORY = JTSFactoryFinder.getGeometryFactory();
    private final LinkedList<GPSPoint> GPSPoints;
    private LineString raw;

    public Trajectory(List<GPSPoint> trajPoints, int size) {
        Assert.assertNotNull(trajPoints);
        GPSPoints = new LinkedList<>();
        trajPoints.sort(Comparator.comparing(GPSPoint::getTimestamp));
        GPSPoints.addAll(trajPoints.subList(0, size));
        raw = buildLine();
    }

    /**
     * build trajectory by gps points
     *
     * @param gpsFc
     * @param idField
     * @param timeField
     * @return
     */
    public static List<GPSPoint> buildGpsPoints(SimpleFeatureCollection gpsFc, String idField, String timeField) {
        Assert.assertNotNull(gpsFc);
        List<GPSPoint> gpsPoints = new ArrayList<>();
        Point prevPoint = null;
        long prevTime = 0L;
        try (SimpleFeatureIterator iterator = gpsFc.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Point point = (Point) feature.getDefaultGeometry();
                long time = Long.parseLong(feature.getProperty(timeField).getValue().toString());
                if (prevTime == time) {
                    System.out.println("dup:" + time);
                    continue;
                }
                String id = feature.getProperty(idField).getValue().toString();
//                LocalDateTime dateTime = LocalDateTime.parse(feature.getProperty(timeField).getValue().toString(), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"));
//                long timestamp = dateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                GPSPoint gpsPoint = new GPSPoint(id, new Timestamp(time), point);
                gpsPoints.add(gpsPoint);
                prevPoint = point;
                prevTime = time;
            }
        }
        return gpsPoints;
    }

    /**
     * build trajectory by gps points
     *
     * @param lineString
     * @return
     */
    public static List<GPSPoint> buildLineStringM(LineString lineString) {
        Assert.assertNotNull(lineString);
        List<GPSPoint> gpsPoints = new ArrayList<>();
        Point prevPoint = null;
        Coordinate[] coordinates = lineString.getCoordinates();
        for (Coordinate coordinate : coordinates) {
            CoordinateXYZM coordinateXYZM = (CoordinateXYZM) coordinate;
            Point point = FACTORY.createPoint(coordinateXYZM);
            if (prevPoint != null && prevPoint.equals(point)) {
                continue;
            }
            long timestamp = (long) coordinateXYZM.getM();
            GPSPoint gpsPoint = new GPSPoint(null, new Timestamp(timestamp), point);
            gpsPoints.add(gpsPoint);
            prevPoint = point;
        }
        return gpsPoints;
    }

    /**
     * build lineString
     *
     * @return
     */
    private LineString buildLine() {
        Assert.assertTrue(GPSPoints.size() > 0);
        Coordinate[] coordinates = new Coordinate[GPSPoints.size()];
        int i = 0;
        for (GPSPoint GPSPoint : GPSPoints) {
            coordinates[i++] = new Coordinate(GPSPoint.getRaw().getX(), GPSPoint.getRaw().getY());
        }
        return FACTORY.createLineString(coordinates);
    }
}

package trajectory;

import lombok.Data;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.Assert;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

    public Trajectory(List<GPSPoint> trajPoints) {
        Assert.assertNotNull(trajPoints);
        GPSPoints = new LinkedList<>();
        trajPoints.sort(Comparator.comparing(GPSPoint::getTimestamp));
        GPSPoints.addAll(trajPoints);
        raw = buildLine();
    }

    public static List<GPSPoint> buildGpsPoints(SimpleFeatureCollection gpsFc, String idField, String timeField) {
        Assert.assertNotNull(gpsFc);
        List<GPSPoint> gpsPoints = new ArrayList<>();
        Point prevPoint = null;
        try (SimpleFeatureIterator iterator = gpsFc.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Point point = (Point) feature.getDefaultGeometry();
                if (prevPoint != null && prevPoint.equals(point)) {
                    continue;
                }
                String id = feature.getProperty(idField).getValue().toString();
                LocalDateTime dateTime = LocalDateTime.parse(feature.getProperty(timeField).getValue().toString(), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"));
                long timestamp = dateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                GPSPoint gpsPoint = new GPSPoint(id, new Timestamp(timestamp), point);
                gpsPoints.add(gpsPoint);
                prevPoint = point;
            }
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

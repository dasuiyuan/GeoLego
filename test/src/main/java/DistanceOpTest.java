import javafx.scene.shape.Polyline;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.operation.distance.DistanceOp;

import java.util.Arrays;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/4/2 18:25
 * @since:
 **/
public class DistanceOpTest {

    private final static WKTReader2 WKT_READER = new WKTReader2();

    private final static WKTWriter2 WKT_WRITER_2 = new WKTWriter2();

    private final static GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();

    public static void main(String[] args) throws ParseException {
        Point pt = GEOMETRY_FACTORY.createPoint(new CoordinateXY(116.36327651023010787, 39.9060352367039286));
        LineString line = (LineString) WKT_READER.read("LineString (116.36091935449613288 39.90589667868272272, 116.36143352161090547 39.90589000118772844, 116.3618341713107327 39.90578316126777736, 116.36244182335546782 39.90584325872274718, 116.36292928049024908 39.90599684110767953, 116.36343009261501891 39.90582990373275862, 116.36387748477982029 39.9058699687027385, 116.36455858926952089 39.90610368102763772, 116.3650326914143136 39.90612371351262766, 116.3650326914143136 39.90612371351262766, 116.36551347105410059 39.90617045597760182)");

        DistanceOp distanceOp = new DistanceOp(line, pt);
        Coordinate coordinate = Arrays.stream(distanceOp.nearestLocations()).findFirst().get().getCoordinate();
        String wkt = WKT_WRITER_2.write(GEOMETRY_FACTORY.createPoint(coordinate));
    }
}

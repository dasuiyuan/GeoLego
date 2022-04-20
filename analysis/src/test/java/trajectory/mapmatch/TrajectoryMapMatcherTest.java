package trajectory.mapmatch;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.WKTWriter2;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.io.helper.GeoReader;
import roadnetwork.model.RoadNetwork;
import roadnetwork.model.RoadSegment;
import trajectory.Trajectory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/4/8 16:56
 * @since:
 **/
public class TrajectoryMapMatcherTest {

    @Test
    public void hmmMapMatch() throws IOException {
        String rnFilePath = "E:\\1-JUST\\6-common\\1-data\\nantong\\nantong_rn_20220324.shp";
        SimpleFeatureCollection rnSfc = GeoReader.ReadShapefile(rnFilePath);
        RoadNetwork roadNetwork = new RoadNetwork(rnSfc);
        String trajFilePath = "E:\\1-JUST\\6-common\\1-data\\nantong\\trajectory_single_sub.shp";
        SimpleFeatureCollection trajSfc = GeoReader.ReadShapefile(trajFilePath);
        Trajectory trajectory = new Trajectory(Trajectory.buildGpsPoints(trajSfc, "id", "time"), 200);
        TrajectoryMapMatcher mapMatcher = new TrajectoryMapMatcher(roadNetwork, trajectory, 50);
        List<CandidatePoint> candidatePoints = mapMatcher.hmmMapMatch();
        WKTWriter2 wktWriter2 = new WKTWriter2();
        for (CandidatePoint candidatePoint : candidatePoints) {
            System.out.println(wktWriter2.write(candidatePoint.getRaw()));
        }
    }

    @Test
    public void hmmMapMatchLineStringM() throws IOException {
        String rnFilePath = "E:\\1-JUST\\6-common\\1-data\\beijing\\roadnetwork\\beijing_rn.shp";
        SimpleFeatureCollection rnSfc = GeoReader.ReadShapefile(rnFilePath);
        RoadNetwork roadNetwork = new RoadNetwork(rnSfc);
//        String trajFilePath = "E:\\1-JUST\\6-common\\1-data\\beijing\\roadnetwork\\beijing_traj_13.shp";
        String trajFilePath = "E:\\1-JUST\\6-common\\1-data\\beijing\\roadnetwork\\beijing_gps_13.shp";
        SimpleFeatureCollection trajSfc = GeoReader.ReadShapefile(trajFilePath);
        Trajectory trajectory = new Trajectory(Trajectory.buildGpsPoints(trajSfc, "id", "time"), 55);
//        SimpleFeatureCollection trajSfc = GeoReader.ReadShapefile(trajFilePath);
//        SimpleFeatureIterator iterator = trajSfc.features();
//        LineString lineString = null;
//        if (iterator.hasNext()) {
//            SimpleFeature simpleFeature = iterator.next();
//            lineString = (LineString) ((Geometry) simpleFeature.getDefaultGeometry()).getGeometryN(0);
//        }
//        Trajectory trajectory = new Trajectory(Trajectory.buildLineStringM(lineString));
        TrajectoryMapMatcher mapMatcher = new TrajectoryMapMatcher(roadNetwork, trajectory, 50);
        List<CandidatePoint> candidatePoints = mapMatcher.hmmMapMatch();
        WKTWriter2 wktWriter2 = new WKTWriter2();
        for (CandidatePoint candidatePoint : candidatePoints) {
            System.out.println(wktWriter2.write(candidatePoint.getRaw()));
        }
//        long startTime = System.currentTimeMillis();
//        List<RoadSegment> roadSegmentLIst = mapMatcher.hmmMapMatchRoad();
//        for (RoadSegment roadSegment : roadSegmentLIst) {
//            System.out.println(roadSegment.getLineString());
//        }
//        System.out.println("mm cost " + (System.currentTimeMillis() - startTime));
    }
}
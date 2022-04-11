package trajectory.mapmatch;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.WKTWriter2;
import org.junit.Test;
import org.sylab.geolego.io.helper.GeoReader;
import roadnetwork.model.RoadNetwork;
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
        Trajectory trajectory = new Trajectory(Trajectory.buildGpsPoints(trajSfc, "id", "time"));
        TrajectoryMapMatcher mapMatcher = new TrajectoryMapMatcher(roadNetwork, trajectory, 30);
        List<CandidatePoint> candidatePoints = mapMatcher.hmmMapMatch();
        WKTWriter2 wktWriter2 = new WKTWriter2();
        for (CandidatePoint candidatePoint : candidatePoints) {
            System.out.println(wktWriter2.write(candidatePoint.getRaw()));
        }
    }
}
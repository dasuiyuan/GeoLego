package trajectory.mapmatch;

import lombok.Data;
import lombok.experimental.Accessors;
import org.locationtech.jts.geom.Point;
import roadnetwork.model.jtsgraph.JTSRoadSegment;
import trajectory.GPSPoint;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/4/7 19:01
 * @since:
 **/
@Data
@Accessors(chain = true)
public class CandidatePoint {
    private Point raw;
    private GPSPoint observationGPSPoint;
    private JTSRoadSegment roadSegment;
    private CandidatePoint prevMaxProbCandidate;
    private double projectDistanceInM;
    private double offsetLengthInM;
}

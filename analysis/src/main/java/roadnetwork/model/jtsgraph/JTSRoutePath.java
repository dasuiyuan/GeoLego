package roadnetwork.model.jtsgraph;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/4/10 8:34
 * @since:
 **/
@AllArgsConstructor
public class JTSRoutePath {
    @Getter
    private List<JTSRoadSegment> routeRoadSegments;
    @Getter
    private double length;

    public JTSRoadSegment getStartRoad() {
        if (routeRoadSegments != null) {
            return routeRoadSegments.get(0);
        }
        return null;
    }

    public JTSRoadSegment getEndRoad() {
        if (routeRoadSegments != null) {
            return routeRoadSegments.get(routeRoadSegments.size() - 1);
        }
        return null;
    }
}

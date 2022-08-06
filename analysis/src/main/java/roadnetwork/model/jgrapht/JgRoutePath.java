package roadnetwork.model.jgrapht;

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
public class JgRoutePath {
    @Getter
    private List<JgRoadSegment> routeRoadSegments;
    @Getter
    private double length;

    public JgRoadSegment getStartRoad() {
        if (routeRoadSegments != null) {
            return routeRoadSegments.get(0);
        }
        return null;
    }

    public JgRoadSegment getEndRoad() {
        if (routeRoadSegments != null) {
            return routeRoadSegments.get(routeRoadSegments.size() - 1);
        }
        return null;
    }
}

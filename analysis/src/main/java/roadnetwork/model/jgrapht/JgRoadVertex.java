package roadnetwork.model.jgrapht;

import lombok.Data;
import org.geotools.graph.structure.Node;
import org.locationtech.jts.geom.Point;

/**
 * @Author: Sui Yuan
 * @Description: road segment vertex
 * @Date: 2022/4/7 9:59
 * @since:
 **/
@Data
public class JgRoadVertex {
    private int rsid;

    private Point raw;
    public JgRoadVertex(Point point, int rsid) {
        this.raw = point;
        this.raw.setUserData(this);
        this.rsid = rsid;
    }
}

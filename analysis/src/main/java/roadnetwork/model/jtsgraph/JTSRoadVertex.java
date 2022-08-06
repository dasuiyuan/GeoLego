package roadnetwork.model.jtsgraph;

import lombok.Data;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.graph.structure.Node;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import javax.swing.plaf.PanelUI;

/**
 * @Author: Sui Yuan
 * @Description: road segment vertex
 * @Date: 2022/4/7 9:59
 * @since:
 **/
@Data
public class JTSRoadVertex {
    private int rsid;
    private Node raw;
    public JTSRoadVertex(Node point, int rsid) {
        this.raw = point;
        this.rsid = rsid;
    }
    public Point getPoint() {
        return (Point) this.raw.getObject();
    }
}

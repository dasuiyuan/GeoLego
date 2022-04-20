package roadnetwork.model;

import lombok.Getter;
import lombok.Setter;
import org.geotools.graph.structure.Edge;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.model.utils.GeoFunction;

/**
 * @Author: Sui Yuan
 * @Description: road segment
 * @Date: 2022/4/7 9:59
 * @since:
 **/
public class RoadSegment {
    @Getter
    @Setter
    private int oid;
    @Getter
    private int id;
    @Getter
    private Edge raw;
    @Getter
    private RoadVertex fromVertex;
    @Getter
    private RoadVertex toVertex;
    @Getter
    @Setter
    private boolean reverse;

    private LineString lineString;
    private double length = 0.0;

    public RoadSegment(int id, Edge edge, RoadVertex fromVertex, RoadVertex toVertex) {
        this.id = id;
        this.raw = edge;
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;
    }

    /**
     * get raw lineString
     *
     * @return
     */
    public LineString getLineString() {
        if (lineString == null) {
            Geometry geom = (Geometry) ((SimpleFeature) this.raw.getObject()).getDefaultGeometry();
            lineString = (LineString) geom.getGeometryN(0);
        }
        return lineString;
    }

    /**
     * get road length
     *
     * @return
     */
    public double getLength() {
        if (this.length == 0.0) {
            LineString lineString = getLineString();
            for (int i = 1; i < lineString.getNumPoints(); i++) {
                this.length += GeoFunction.getDistanceInM(lineString.getCoordinateN(i - 1), lineString.getCoordinateN(i));
            }
        }
        return this.length;
    }
}

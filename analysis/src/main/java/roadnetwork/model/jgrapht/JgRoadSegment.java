package roadnetwork.model.jgrapht;

import lombok.Getter;
import lombok.Setter;
import org.geotools.graph.structure.Edge;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.index.utils.GeometryUtils;
import org.sylab.geolego.model.utils.GeoFunction;

import java.util.Objects;

/**
 * @Author: Sui Yuan
 * @Description: road segment
 * @Date: 2022/4/7 9:59
 * @since:
 **/
public class JgRoadSegment {
    @Getter
    private int id;
    @Getter
    private LineString raw;
    @Getter
    private JgRoadVertex fromVertex;
    @Getter
    private JgRoadVertex toVertex;
    @Getter
    @Setter
    private boolean reverse;

    private double length = 0.0;

    private JgRoadSegment reverseRs;

    public JgRoadSegment(int id, LineString edge, JgRoadVertex fromVertex, JgRoadVertex toVertex) {
        this.id = id;
        this.raw = edge;
        this.raw.setUserData(this);
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;
    }

    /**
     * get road length
     *
     * @return
     */
    public double getLength() {
        if (this.length == 0.0) {
            LineString lineString = raw;
            for (int i = 1; i < lineString.getNumPoints(); i++) {
                this.length += GeoFunction.getDistanceInM(lineString.getCoordinateN(i - 1), lineString.getCoordinateN(i));
            }
        }
        return this.length;
    }

    /**
     * 获取反向
     *
     * @return
     */
    public JgRoadSegment getReverse() {
        if (reverseRs == null) {
            Coordinate[] coordinates = this.raw.getCoordinates();
            Coordinate[] revCoordinates = new Coordinate[coordinates.length];
            int j = 0;
            for (int i = revCoordinates.length - 1; i >= 0; i--) {
                revCoordinates[j++] = coordinates[i];
            }
            LineString lineString = GeometryUtils.GEOMETRY_FACTORY.createLineString(revCoordinates);
            reverseRs = new JgRoadSegment(this.id * (-1), lineString, toVertex, fromVertex);
        }
        return reverseRs;
    }

    public JgRoadVertex getAnotherVertex(JgRoadVertex vertex) {
        if (vertex.getRsid() == this.fromVertex.getRsid()) {
            return toVertex;
        }
        return fromVertex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JgRoadSegment that = (JgRoadSegment) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

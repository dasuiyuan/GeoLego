package roadnetwork.model.jgrapht;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.index.rtree.RTreeIndexOper;
import org.sylab.geolego.model.utils.GeoFunction;
import roadnetwork.RoadGraphProperty;
import roadnetwork.model.jtsgraph.JTSRoadSegment;
import roadnetwork.model.jtsgraph.JTSRoadVertex;
import scala.Tuple2;

import java.util.HashMap;
import java.util.Set;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/5/4 16:43
 * @since:
 **/
@Slf4j
public class JgRoadNetwork {

    @Getter
    private final RTreeIndexOper rTreeSegment = new RTreeIndexOper();

    private final RTreeIndexOper rTreeVertex = new RTreeIndexOper();

    private final HashMap<Integer, JgRoadSegment> roadSegmentMap = new HashMap<>();

    private final HashMap<Integer, JgRoadVertex> roadVertexMap = new HashMap<>();

    private JgRoadGraph graph;


    public JgRoadNetwork(SimpleFeatureCollection featureCollection, RoadGraphProperty rnProp) {
        this.graph = new JgRoadGraph(featureCollection, rnProp);
        buildRTreeIndex();
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> dijkstraShortestPath(JgRoadVertex start, JgRoadVertex end) {
        return this.graph.dijkstraShortestPath(start, end);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> dijkstraShortestPath(Point start, Point end) {
        Tuple2<JgRoadVertex, JgRoadVertex> tuple2 = getNNVertex(start, end);
        return dijkstraShortestPath(tuple2._1, tuple2._2);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> biDijkstraShortestPath(JgRoadVertex start, JgRoadVertex end) {
        return this.graph.biDijkstraShortestPath(start, end);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> biDijkstraShortestPath(Point start, Point end) {
        Tuple2<JgRoadVertex, JgRoadVertex> tuple2 = getNNVertex(start, end);
        return biDijkstraShortestPath(tuple2._1, tuple2._2);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> astarShortestPath(JgRoadVertex start, JgRoadVertex end) {
        return this.graph.astarShortestPath(start, end);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> astarShortestPath(Point start, Point end) {
        Tuple2<JgRoadVertex, JgRoadVertex> tuple2 = getNNVertex(start, end);
        return astarShortestPath(tuple2._1, tuple2._2);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> biAstarShortestPath(JgRoadVertex start, JgRoadVertex end) {
        return this.graph.biAstarShortestPath(start, end);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> biAstarShortestPath(Point start, Point end) {
        Tuple2<JgRoadVertex, JgRoadVertex> tuple2 = getNNVertex(start, end);
        return biAstarShortestPath(tuple2._1, tuple2._2);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> justAstarShortestPath(JgRoadVertex start, JgRoadVertex end) {
        return this.graph.justAstarShortestPath(start, end);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> justAstarShortestPath(Point start, Point end) {
        Tuple2<JgRoadVertex, JgRoadVertex> tuple2 = getNNVertex(start, end);
        return justAstarShortestPath(tuple2._1, tuple2._2);
    }

    public Envelope extend(Point point) {
        Envelope extendedEnv = point.getEnvelopeInternal();
        extendedEnv.expandBy(GeoFunction.getDegreeFromM(100));
        return extendedEnv;
    }

    private Tuple2<JgRoadVertex, JgRoadVertex> getNNVertex(Point start, Point end) {
        long knnStart = System.currentTimeMillis();
        JgRoadVertex startVertex = (JgRoadVertex) this.rTreeVertex.knn(extend(start), start, 1).get(0).getUserData();
        JgRoadVertex endVertex = (JgRoadVertex) this.rTreeVertex.knn(extend(end), end, 1).get(0).getUserData();
        System.out.println("knn cost: " + (System.currentTimeMillis() - knnStart));
        return new Tuple2<>(startVertex, endVertex);
    }

    private void buildRTreeIndex() {
        Set<JgRoadVertex> vertexSet = this.graph.getGraph().vertexSet();
        Set<JgRoadSegment> segmentSet = this.graph.getGraph().edgeSet();
        for (JgRoadVertex jgRoadVertex : vertexSet) {
            roadVertexMap.put(jgRoadVertex.getRsid(), jgRoadVertex);
            rTreeVertex.add(jgRoadVertex.getRaw());
        }
        for (JgRoadSegment jgRoadSegment : segmentSet) {
            roadSegmentMap.put(jgRoadSegment.getId(), jgRoadSegment);
            rTreeSegment.add(jgRoadSegment.getRaw());
        }
        this.rTreeSegment.buildIndex();
        this.rTreeVertex.buildIndex();
    }
}

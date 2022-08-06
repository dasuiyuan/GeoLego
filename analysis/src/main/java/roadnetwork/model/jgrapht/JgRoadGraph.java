package roadnetwork.model.jgrapht;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.alg.shortestpath.BidirectionalAStarShortestPath;
import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import roadnetwork.EnumRNDirection;
import roadnetwork.RoadGraphProperty;
import roadnetwork.astar.AStarOperator;
import roadnetwork.astar.AStarOperatorJgrapht;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/5/6 11:29
 * @since:
 **/
@Slf4j
public class JgRoadGraph {
    private final Map<Coordinate, JgRoadVertex> vertexMap = new HashMap<>();
    @Getter
    private Graph<JgRoadVertex, JgRoadSegment> graph;
    private AtomicInteger edgeId = new AtomicInteger();
    private AtomicInteger vertexId = new AtomicInteger();


    public JgRoadGraph(SimpleFeatureCollection featureCollection, RoadGraphProperty rgProp) {
        GraphTypeBuilder baseGraphBuilder = rgProp.isDirected() ? GraphTypeBuilder.directed() : GraphTypeBuilder.undirected();
        graph = baseGraphBuilder.weighted(true)
                .allowingMultipleEdges(false)
                .allowingSelfLoops(true)
                .vertexClass(JgRoadVertex.class)
                .edgeClass(JgRoadSegment.class)
                .buildGraph();
        buildGraphContent(featureCollection, rgProp);
    }

    private void buildGraphContent(SimpleFeatureCollection featureCollection, RoadGraphProperty rgProp) {
        try (SimpleFeatureIterator iterator = featureCollection.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                LineString lineString = (LineString) geometry.getGeometryN(0);
                Point fromPoint = lineString.getStartPoint();
                Point toPoint = lineString.getEndPoint();

                //try to find exist vertex, create new one if absent
                JgRoadVertex jgRoadVertexFrom = vertexMap.get(fromPoint.getCoordinate());
                if (jgRoadVertexFrom == null) {
                    jgRoadVertexFrom = new JgRoadVertex(fromPoint, vertexId.incrementAndGet());
                    this.vertexMap.put(fromPoint.getCoordinate(), jgRoadVertexFrom);
                }
                JgRoadVertex jgRoadVertexTo = vertexMap.get(toPoint.getCoordinate());
                if (jgRoadVertexTo == null) {
                    jgRoadVertexTo = new JgRoadVertex(toPoint, vertexId.incrementAndGet());
                    this.vertexMap.put(toPoint.getCoordinate(), jgRoadVertexTo);
                }

                JgRoadSegment jgRoadSegment = new JgRoadSegment(edgeId.incrementAndGet(), lineString, jgRoadVertexFrom, jgRoadVertexTo);
                graph.addVertex(jgRoadVertexFrom);
                graph.addVertex(jgRoadVertexTo);
                if (rgProp.isDirected()) {
                    String dirField = rgProp.getDirectionField();
                    int direct = Integer.parseInt(feature.getAttribute(dirField).toString());
                    switch (direct) {
                        case EnumRNDirection.backward:
                            addEdge(jgRoadVertexTo, jgRoadVertexFrom, jgRoadSegment.getReverse());
                            break;
                        case EnumRNDirection.both:
                            addEdge(jgRoadVertexFrom, jgRoadVertexTo, jgRoadSegment);
                            addEdge(jgRoadVertexTo, jgRoadVertexFrom, jgRoadSegment.getReverse());
                            break;
                        case EnumRNDirection.forward:
                        default:
                            addEdge(jgRoadVertexFrom, jgRoadVertexTo, jgRoadSegment);
                            break;
                    }
                } else {
                    addEdge(jgRoadVertexFrom, jgRoadVertexTo, jgRoadSegment);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> dijkstraShortestPath(JgRoadVertex startVertex, JgRoadVertex endVertex) {
        DijkstraShortestPath<JgRoadVertex, JgRoadSegment> dijkstraShortestPath = new DijkstraShortestPath(this.graph);
        return dijkstraShortestPath.getPath(startVertex, endVertex);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> biDijkstraShortestPath(JgRoadVertex startVertex, JgRoadVertex endVertex) {
        BidirectionalDijkstraShortestPath<JgRoadVertex, JgRoadSegment> dijkstraShortestPath = new BidirectionalDijkstraShortestPath<>(this.graph);
        return dijkstraShortestPath.getPath(startVertex, endVertex);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> astarShortestPath(JgRoadVertex startVertex, JgRoadVertex endVertex) {
        AStarShortestPath<JgRoadVertex, JgRoadSegment> astarShortestPath = new AStarShortestPath<>(graph, new AStarAdmissibleHeuristic<JgRoadVertex>() {
            @Override
            public double getCostEstimate(JgRoadVertex sourceVertex, JgRoadVertex targetVertex) {
                return 0;
            }
        });
        return astarShortestPath.getPath(startVertex, endVertex);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> biAstarShortestPath(JgRoadVertex startVertex, JgRoadVertex endVertex) {
        BidirectionalAStarShortestPath<JgRoadVertex, JgRoadSegment> astarShortestPath = new BidirectionalAStarShortestPath<>(graph, new AStarAdmissibleHeuristic<JgRoadVertex>() {
            @Override
            public double getCostEstimate(JgRoadVertex sourceVertex, JgRoadVertex targetVertex) {
                return sourceVertex.getRaw().distance(targetVertex.getRaw());
            }
        });
        return astarShortestPath.getPath(startVertex, endVertex);
    }

    public GraphPath<JgRoadVertex, JgRoadSegment> justAstarShortestPath(JgRoadVertex startVertex, JgRoadVertex endVertex) {
        AStarOperatorJgrapht aStarShortestPath = new AStarOperatorJgrapht(graph);
        return aStarShortestPath.getPath(startVertex, endVertex);
    }

    public String printGraph() {
        return this.graph.toString();
    }

    private void addEdge(JgRoadVertex fromVertex, JgRoadVertex toVertex, JgRoadSegment segment) {
        graph.addEdge(fromVertex, toVertex, segment);
        graph.setEdgeWeight(segment, segment.getLength());
    }
}

package roadnetwork;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.path.AStarShortestPathFinder;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.traverse.standard.AStarIterator;
import org.geotools.graph.traverse.standard.AStarIterator.AStarFunctions;
import org.geotools.graph.traverse.standard.DijkstraIterator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

import java.util.List;

/**
 * @author Sui Yuan
 * @description
 * @date 2020/12/11 15:53
 */
@Slf4j
public class Router {

    /**
     * 障碍区域
     */
    private List<Polygon> barriers;

    @Getter
    private Graph graph;

    private RoadGraphProperty routeProperty;

    public Router(SimpleFeatureCollection featureCollection, List<Polygon> barriers, RoadGraphProperty routeProperty) {
        this.barriers = barriers;
        this.graph = buildGraph(featureCollection);
        this.routeProperty = routeProperty;
    }
    /**
     * @return
     */
    protected Graph buildGraph(SimpleFeatureCollection featureCollection) {
        LineStringGraphGenerator lineStringGraphGenerator = new LineStringGraphGenerator();
        FeatureGraphGenerator featureGraphGenerator = new FeatureGraphGenerator(lineStringGraphGenerator);

        try (SimpleFeatureIterator iterator = featureCollection.features()) {
            while (iterator.hasNext()) {
                Feature feature = iterator.next();
                featureGraphGenerator.add(feature);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return featureGraphGenerator.getGraph();
    }

    /**
     * dijkstra最短路径
     *
     * @param startNode 起始节点
     * @param endNode   终止节点
     * @return
     */
    public Path searchRouteDijkstra(Node startNode, Node endNode) {
        if (graph == null) {
            return null;
        }
        DijkstraIterator.EdgeWeighter weighter = this::dijkstraCost;
        DijkstraShortestPathFinder pf = new DijkstraShortestPathFinder(graph, startNode, weighter);
        pf.calculate();
        return pf.getPath(endNode);
    }


    /**
     * 计算边的花费
     *
     * @param e 边
     * @return 花费值
     */
    public double dijkstraCost(Edge e) {
        SimpleFeature feature = (SimpleFeature) e.getObject();
        Geometry geom = (Geometry) feature.getDefaultGeometry();
        //geom.convexHull()将其构成一个图形
        if (barriers != null) {
            for (int i = 0; i < barriers.size(); i++) {
                Geometry g = barriers.get(i);
                if (geom.intersects(g)) {
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
        if (this.routeProperty.getCostField() != null && !this.routeProperty.getCostField().isEmpty()) {
            return Double.parseDouble(feature.getAttribute(routeProperty.getCostField()).toString());
        }
        return geom.getLength();
    }

    /**
     * astar最短路径
     *
     * @param startNode 起始节点
     * @param endNode   终止节点
     * @return
     */
    public Path searchRouteAStar(Node startNode, Node endNode) throws Exception {
        if (graph == null) {
            return null;
        }
        AStarShortestPathFinder pf = new AStarShortestPathFinder(graph,
                startNode,
                endNode, new AStarFunctions(endNode) {
            @Override
            public double cost(AStarIterator.AStarNode n1, AStarIterator.AStarNode n2) {
                return aStarCost(n1, n2);
            }

            @Override
            public double h(Node n) {
                Point desPoint = (Point) this.getDest().getObject();
                return ((Point) n.getObject()).distance(desPoint);
            }
        });
        pf.calculate();
        return pf.getPath();
    }

    /**
     * AStar算法成本计算
     *
     * @param starANode 起点
     * @param endANode  终点
     * @return
     */
    public double aStarCost(AStarIterator.AStarNode starANode, AStarIterator.AStarNode endANode) {
        Node starNode = starANode.getNode();
        Node endNode = endANode.getNode();
        Edge edge = starNode.getEdge(endNode);
        if (edge != null) {
            SimpleFeature simpleFeature = (SimpleFeature) edge.getObject();
            Geometry edgeGeom = (Geometry) simpleFeature.getDefaultGeometry();
            if (barriers != null) {
                //监测障碍区
                for (Polygon barrier : barriers) {
                    if (barrier.intersects(edgeGeom)) {
                        return Double.POSITIVE_INFINITY;
                    }
                }
            }

            //判断方向
//            String direction = simpleFeature.getAttribute(routeProperty.getDirectionField()).toString();
//            switch (direction) {
//                //双向
//                case "B":
//                    return edgeGeom.getLength();
//                //反向
//                case "T": {
//                    if (edge.getNodeA().equals(starNode)) {
//                        return Double.POSITIVE_INFINITY;
//                    } else {
//                        return edgeGeom.getLength();
//                    }
//                }
//                //正向
//                case "F": {
//                    //
//                    if (edge.getNodeA().equals(endNode)) {
//                        return Double.POSITIVE_INFINITY;
//                    } else {
//                        return edgeGeom.getLength();
//                    }
//                }
//
//            }

            return edgeGeom.getLength();
        }
        return ((Geometry) starNode.getObject()).distance((Geometry) endNode.getObject());
    }

    /**
     * 查找最近节点
     *
     * @param point
     * @return
     */
    public Node getNearestGraphNode(Point point) {
        if (graph == null) {
            System.out.println("graph不存在，请构建graph");
            return null;
        }
        double dist = 0;
        Node nearestNode = null;
        for (Object o : graph.getNodes()) {
            Node n = (Node) o;
            Point gPoint = (Point) n.getObject();
            double distance = gPoint.distance(point);
            if (nearestNode == null || distance < dist) {
                dist = distance;
                nearestNode = n;
            }
        }
        return nearestNode;
    }
}

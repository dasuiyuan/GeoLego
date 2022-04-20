package roadnetwork.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.junit.Assert;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.index.rtree.RTreeIndexOper;
import org.sylab.geolego.model.utils.GeoFunction;
import roadnetwork.DirectedDuplicateRouter;
import roadnetwork.RouteProperty;
import roadnetwork.Router;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: Sui Yuan
 * @Description: road network object
 * @Date: 2022/4/7 9:58
 * @since:
 **/
@Slf4j
public class RoadNetwork {
    @Getter
    private RTreeIndexOper rTree;
    private Router router;
    private HashMap<Integer, RoadSegment> roadSegmentMap;
    private HashMap<Integer, RoadVertex> roadVertexMap;


    public RoadNetwork(SimpleFeatureCollection featureCollection) {
        this.rTree = new RTreeIndexOper();
        this.router = new DirectedDuplicateRouter(featureCollection, null, new RouteProperty(null, null));
        this.roadSegmentMap = new HashMap<>(this.router.getGraph().getEdges().size());
        this.roadVertexMap = new HashMap<>(this.router.getGraph().getNodes().size());
        initRoadNetwork();
    }

    private void initRoadNetwork() {
        Assert.assertNotNull(router.getGraph());
        Graph graph = router.getGraph();
        for (Edge edge : graph.getEdges()) {
            Node nodeA = edge.getNodeA();
            Node nodeB = edge.getNodeB();
            RoadVertex rvA = this.roadVertexMap.get(nodeA.getID());
            RoadVertex rvB = this.roadVertexMap.get(nodeB.getID());
            if (rvA == null) {
                rvA = new RoadVertex(nodeA, nodeA.getID());
                this.roadVertexMap.put(nodeA.getID(), rvA);
            }
            if (rvB == null) {
                rvB = new RoadVertex(nodeB, nodeB.getID());
                this.roadVertexMap.put(nodeB.getID(), rvB);
            }
            RoadSegment roadSegment = this.roadSegmentMap.get(edge.getID());
            if (roadSegment == null) {
                roadSegment = new RoadSegment(edge.getID(), edge, rvA, rvB);
                this.roadSegmentMap.put(edge.getID(), roadSegment);
            }
            //add roadsegment linestring into rtree
            SimpleFeature simpleFeature = (SimpleFeature) edge.getObject();
            Geometry roadGeom = (Geometry) simpleFeature.getDefaultGeometry();
            boolean isReverse = roadGeom.getUserData() instanceof Boolean&&(boolean)roadGeom.getUserData();
            roadSegment.setReverse(isReverse);
            //todo: set oid field as a param
            int oid = Integer.parseInt(simpleFeature.getAttribute(1).toString());
            oid = isReverse ? oid * (-1) : oid;
            roadSegment.setOid(oid);
            roadGeom.setUserData(roadSegment);
            rTree.add(roadGeom);
        }
        rTree.buildIndex();
    }

    /**
     * Get shortest path from startnode to endNode, then return the roadSegment collection
     *
     * @param startNode
     * @param endNode
     * @return
     */
    public RoutePath shortestPath(Node startNode, Node endNode) {
        List<RoadSegment> roadSegments = new LinkedList<>();
        Path aPath = null;
        try {
            aPath = router.searchRouteAStar(startNode, endNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        double length = 0.0;
        for (Object edge : aPath.getEdges()) {
            Edge curEdge = (Edge) edge;
            roadSegments.add(this.roadSegmentMap.get(curEdge.getID()));
            LineString lineString = (LineString) ((Geometry) ((SimpleFeature) curEdge.getObject()).getDefaultGeometry()).getGeometryN(0);
            length += GeoFunction.getDistanceInM(lineString);
        }
        return new RoutePath(roadSegments, length);
    }
}

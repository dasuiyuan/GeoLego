package roadnetwork.model.jgrapht;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.graph.structure.Node;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.junit.Test;
import org.locationtech.geomesa.utils.interop.WKTUtils;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Point;
import org.sylab.geolego.index.utils.GeometryUtils;
import org.sylab.geolego.io.helper.GeoReader;
import roadnetwork.RoadGraphProperty;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/5/5 20:55
 * @since:
 **/
public class JgRoadNetworkTest {

    @Test
    public void roadnetworkTest() throws IOException {
//        String filePath = "E:\\1-JUST\\6-common\\1-data\\nantong\\nantong_rn_20220324.shp";
        String filePath = "E:\\1-JUST\\6-common\\1-data\\beijing\\roadnetwork\\beijing_rn.shp";
        SimpleFeatureCollection simpleFeatureCollection = GeoReader.ReadShapefile(filePath);
        RoadGraphProperty routeProperty = new RoadGraphProperty();
        long startBuildTime = System.currentTimeMillis();
        JgRoadNetwork jgRoadNetwork = new JgRoadNetwork(simpleFeatureCollection, routeProperty);
        System.out.println("build graph cost: " + (System.currentTimeMillis() - startBuildTime));
//        Point start = GeometryUtils.GEOMETRY_FACTORY.createPoint(new CoordinateXY(120.44588, 32.46976));
//        Point end = GeometryUtils.GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.70537, 31.85340));
        Point start = GeometryUtils.GEOMETRY_FACTORY.createPoint(new CoordinateXY(116.30352,39.85806));
        Point end = GeometryUtils.GEOMETRY_FACTORY.createPoint(new CoordinateXY(116.462202,39.979428));
//        long startSp = System.currentTimeMillis();
//        GraphPath<JgRoadVertex, JgRoadSegment> path = jgRoadNetwork.dijkstraShortestPath(start, end);
//        long cost = System.currentTimeMillis() - startSp;
//        System.out.println("dij shortest path cost: " + cost + " length:" + path.getWeight());
////        for (JgRoadSegment jgRoadSegment : path.getEdgeList()) {
////            System.out.println(WKTUtils.write(jgRoadSegment.getRaw()));
////        }
//
        for (int i = 0; i < 10; i++) {

            long startSBiDij = System.currentTimeMillis();
            GraphPath<JgRoadVertex, JgRoadSegment> pathBiDij = jgRoadNetwork.biDijkstraShortestPath(start, end);
            long costAstarDij = System.currentTimeMillis() - startSBiDij;
            System.out.println("bidji shortest path cost: " + costAstarDij + " length:" + pathBiDij.getWeight());
//        for (JgRoadSegment jgRoadSegment : pathAstarBiDij.getEdgeList()) {
//            System.out.println(WKTUtils.write(jgRoadSegment.getRaw()));
//        }

            long startSpAstar = System.currentTimeMillis();
            GraphPath<JgRoadVertex, JgRoadSegment> pathAstar = jgRoadNetwork.astarShortestPath(start, end);
            long costAstar = System.currentTimeMillis() - startSpAstar;
            System.out.println("astar shortest path cost: " + costAstar + " length:" + pathAstar.getWeight());
//        for (JgRoadSegment jgRoadSegment : pathAstar.getEdgeList()) {
//            System.out.println(WKTUtils.write(jgRoadSegment.getRaw()));
//        }

            long startSpAstarJust = System.currentTimeMillis();
            GraphPath<JgRoadVertex, JgRoadSegment> pathAstarJust = jgRoadNetwork.justAstarShortestPath(start, end);
            long costAstarJust = System.currentTimeMillis() - startSpAstarJust;
            System.out.println("just astar shortest path cost: " + costAstarJust + " length:" + pathAstarJust.getWeight());
//        for (JgRoadSegment jgRoadSegment : pathAstar.getEdgeList()) {
//            System.out.println(WKTUtils.write(jgRoadSegment.getRaw()));
//        }
            System.out.println("----------------------------------------------------------");
        }

    }


    @Test
    public void testJgrapht() throws URISyntaxException {
        Graph<URI, DefaultEdge> g = createHrefGraph();
        URI start = g.vertexSet().stream().filter(v -> {
            try {
                return v.toURL().getHost().equals("www.google.com");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return false;
        }).findAny().get();

        dfsTraverse(g, start);
    }

    private Graph<URI, DefaultEdge> createHrefGraph() throws URISyntaxException {
        Graph<URI, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        URI google = new URI("http://www.google.com");
        URI wikipedia = new URI("http://www.wikipedia.org");
        URI jgrapht = new URI("http://jgrapht.org");

        g.addVertex(google);
        g.addVertex(wikipedia);
        g.addVertex(jgrapht);

        g.addEdge(google, jgrapht);
        g.addEdge(google, wikipedia);
        g.addEdge(jgrapht, wikipedia);
        g.addEdge(wikipedia, google);

        return g;
    }

    private void dfsTraverse(Graph<URI, DefaultEdge> g, URI start) {
        Iterator<URI> iterator = new DepthFirstIterator<>(g, start);
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}
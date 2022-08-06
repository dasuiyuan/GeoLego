package roadnetwork;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.WKTWriter2;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.index.utils.GeometryUtils;
import org.sylab.geolego.io.helper.GeoReader;
import roadnetwork.astar.AStarOperator;

import java.util.List;

public class RouterTest {

    private static GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();

    @Test
    public void routeShp() throws Exception {
        String filePath = "E:\\1-JUST\\6-common\\1-data\\nantong\\nantong_rn_20220324.shp";
//        String filePath = "E:\\1-JUST\\6-common\\1-data\\beijing\\roadnetwork\\beijing_rn.shp";
        SimpleFeatureCollection simpleFeatureCollection = GeoReader.ReadShapefile(filePath);
        RoadGraphProperty routeProperty = new RoadGraphProperty();
        long startBuildTime = System.currentTimeMillis();
        Router router = new Router(simpleFeatureCollection, null, routeProperty);
        System.out.println("build graph cost: " + (System.currentTimeMillis() - startBuildTime));

        long startFindNearestTime = System.currentTimeMillis();
        Node start = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(120.44588, 32.46976)));
        Node end = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.70537, 31.85340)));
//        Node start = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(116.30352,39.85806)));
//        Node end = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(116.462202,39.979428)));
        System.out.println("find nearest cost: " + (System.currentTimeMillis() - startFindNearestTime));

        WKTWriter2 wktWriter2 = new WKTWriter2();

//        long startDijTime = System.currentTimeMillis();
//        Path djPath = router.searchRouteDijkstra(start, end);
//        System.out.println("dijkstra cost: " + (System.currentTimeMillis() - startDijTime));
//        for (Object edge : djPath.getEdges()) {
//            System.out.println(wktWriter2.write((Geometry) ((SimpleFeature) ((Edge) edge).getObject()).getDefaultGeometry()));
//        }

        for (int i = 0; i < 10; i++) {

            long startAStarTime = System.currentTimeMillis();
            Path aPath = router.searchRouteAStar(start, end);
            System.out.println("geotools astar cost: " + (System.currentTimeMillis() - startAStarTime));
//        for (Object edge : aPath.getEdges()) {
//            System.out.println(wktWriter2.write((Geometry) ((SimpleFeature) ((Edge) edge).getObject()).getDefaultGeometry()));
//        }

            long startAStarTime1 = System.currentTimeMillis();
            List<Edge> edgeList = AStarOperator.findShortestPath(start, end);
            System.out.println("self astar cost: " + (System.currentTimeMillis() - startAStarTime1));
//        WKTWriter2 wktWriter2 = new WKTWriter2();
//        for (Object edge : edgeList) {
//            System.out.println(wktWriter2.write((Geometry) ((SimpleFeature) ((Edge) edge).getObject()).getDefaultGeometry()));
//        }
        }
    }

    @Test
    public void directedRoute() throws Exception {
        String filePath = "E:\\1-JUST\\6-common\\1-data\\nantong\\nantong_rn_20220324.shp";
//        String filePath = "E:\\3-Personal\\3-exercise\\barefoot\\data\\oberbayern-latest-free.shp\\gis_osm_roads_free_1.shp";

        SimpleFeatureCollection simpleFeatureCollection = GeoReader.ReadShapefile(filePath);
        RoadGraphProperty routeProperty = new RoadGraphProperty();
        long startBuildTime = System.currentTimeMillis();
        DirectedRouter router = new DirectedRouter(simpleFeatureCollection, null, routeProperty);
        System.out.println("build graph cost: " + (System.currentTimeMillis() - startBuildTime));

        long startFindNearestTime = System.currentTimeMillis();
//        Node start = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(11.562849999, 48.16087799)));
//        Node end = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(11.5633480, 48.1555872)));
        Node start = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.1372008, 32.3064821)));
        Node end = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.1338570, 32.3065655)));
        System.out.println("find nearest cost: " + (System.currentTimeMillis() - startFindNearestTime));

//        long startDijTime = System.currentTimeMillis();
//        Path djPath = router.searchRouteDijkstra(start, end);
//        System.out.println("dijkstra cost: " + (System.currentTimeMillis() - startDijTime));
//        System.out.println(djPath);

        long startAStarTime = System.currentTimeMillis();
        Path aPath = router.searchRouteAStar(start, end);
        System.out.println("astar cost: " + (System.currentTimeMillis() - startAStarTime));
        WKTWriter2 wktWriter2 = new WKTWriter2();
        for (Object edge : aPath.getEdges()) {
            System.out.println(wktWriter2.write((Geometry) ((SimpleFeature) ((Edge) edge).getObject()).getDefaultGeometry()));
        }
    }

}
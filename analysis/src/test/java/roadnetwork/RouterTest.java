package roadnetwork;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.WKTWriter2;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.io.helper.GeoReader;

import java.io.IOException;

import static org.junit.Assert.*;

public class RouterTest {

    private static GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();

    @Test
    public void routeShp() throws Exception {
        String filePath = "E:\\1-JUST\\6-common\\1-data\\nantong\\nantong_rn_20220324.shp";
        SimpleFeatureCollection simpleFeatureCollection = GeoReader.ReadShapefile(filePath);
        RouteProperty routeProperty = new RouteProperty();
        long startBuildTime = System.currentTimeMillis();
        Router router = new Router(simpleFeatureCollection, null, routeProperty);
        System.out.println("build graph cost: " + (System.currentTimeMillis() - startBuildTime));

        long startFindNearestTime = System.currentTimeMillis();
        Node start = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(120.620,32.237)));
        Node end = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.543,31.950)));
        System.out.println("find nearest cost: " + (System.currentTimeMillis() - startFindNearestTime));

        WKTWriter2 wktWriter2 = new WKTWriter2();

        long startDijTime = System.currentTimeMillis();
        Path djPath = router.searchRouteDijkstra(start, end);
        System.out.println("dijkstra cost: " + (System.currentTimeMillis() - startDijTime));
        for (Object edge : djPath.getEdges()) {
            System.out.println(wktWriter2.write((Geometry) ((SimpleFeature) ((Edge) edge).getObject()).getDefaultGeometry()));
        }

//        long startAStarTime = System.currentTimeMillis();
//        Path aPath = router.searchRouteAStar(start, end);
//        System.out.println("astar cost: " + (System.currentTimeMillis() - startAStarTime));
//        for (Object edge : aPath.getEdges()) {
//            System.out.println(wktWriter2.write((Geometry) ((SimpleFeature) ((Edge) edge).getObject()).getDefaultGeometry()));
//        }
    }

    @Test
    public void directedRoute() throws Exception {
//        String filePath = "E:\\1-JUST\\2-gis\\1-data\\nantong\\nantong-Incar\\road\\Rnantong.shp";
        String filePath = "E:\\3-Personal\\3-exercise\\barefoot\\data\\oberbayern-latest-free.shp\\gis_osm_roads_free_1.shp";

        SimpleFeatureCollection simpleFeatureCollection = GeoReader.ReadShapefile(filePath);
        RouteProperty routeProperty = new RouteProperty();
        long startBuildTime = System.currentTimeMillis();
        DirectedRouter router = new DirectedRouter(simpleFeatureCollection, null, routeProperty);
        System.out.println("build graph cost: " + (System.currentTimeMillis() - startBuildTime));

        long startFindNearestTime = System.currentTimeMillis();
        Node start = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(11.562849999, 48.16087799)));
        Node end = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(11.5633480,48.1555872)));
//        Node start = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.656247, 31.614414)));
//        Node end = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.654864, 31.603724)));
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
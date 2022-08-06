package roadnetwork.dijkstra;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.WKTWriter2;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.io.helper.GeoReader;
import roadnetwork.RoadGraphProperty;
import roadnetwork.Router;

import java.io.IOException;
import java.util.List;

public class DijkstarOperatorTest {

    private static GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();

    @Test
    public void findShortestPath() throws IOException {
        String filePath = "E:\\1-JUST\\6-common\\1-data\\nantong\\nantong_rn_20220324.shp";
        SimpleFeatureCollection simpleFeatureCollection = GeoReader.ReadShapefile(filePath);
        RoadGraphProperty routeProperty = new RoadGraphProperty();
        long startBuildTime = System.currentTimeMillis();
        Router router = new Router(simpleFeatureCollection, null, routeProperty);
        System.out.println("build graph cost: " + (System.currentTimeMillis() - startBuildTime));

        long startFindNearestTime = System.currentTimeMillis();
        Node start = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.15556781226152, 32.320211371935955)));
        Node end = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.15476606942587523, 32.3223699944434415)));
        System.out.println("find nearest cost: " + (System.currentTimeMillis() - startFindNearestTime));

        List<Edge> edgeList = DijkstarOperator.findShortestPath(start, end);
        WKTWriter2 wktWriter2 = new WKTWriter2();
        for (Object edge : edgeList) {
            System.out.println(wktWriter2.write((Geometry) ((SimpleFeature) ((Edge) edge).getObject()).getDefaultGeometry()));
        }
    }
}
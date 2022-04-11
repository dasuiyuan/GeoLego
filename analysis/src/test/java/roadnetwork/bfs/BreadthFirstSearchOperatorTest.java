package roadnetwork.bfs;

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
import roadnetwork.RouteProperty;
import roadnetwork.Router;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class BreadthFirstSearchOperatorTest {

    private static GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();


    @Test
    public void findPath() throws IOException {
        String filePath = "E:\\1-JUST\\2-gis\\1-data\\nantong\\nantong-Incar\\road\\Rnantong.shp";
        SimpleFeatureCollection simpleFeatureCollection = GeoReader.ReadShapefile(filePath);
        RouteProperty routeProperty = new RouteProperty();
        long startBuildTime = System.currentTimeMillis();
        Router router = new Router(simpleFeatureCollection, null, routeProperty);
        System.out.println("build graph cost: " + (System.currentTimeMillis() - startBuildTime));

        long startFindNearestTime = System.currentTimeMillis();
        Node start = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.656247, 31.614414)));
        Node end = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.654864, 31.603724)));
        System.out.println("find nearest cost: " + (System.currentTimeMillis() - startFindNearestTime));

        List<Edge> edgeList = BreadthFirstSearchOperator.FindPath(start, end);
        WKTWriter2 wktWriter2 = new WKTWriter2();
        for (Object edge : edgeList) {
            System.out.println(wktWriter2.write((Geometry) ((SimpleFeature) ((Edge) edge).getObject()).getDefaultGeometry()));
        }
    }
}
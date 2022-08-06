package roadnetwork;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.WKTWriter2;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.io.helper.GeoReader;

import static org.sylab.geolego.io.helper.GeoReader.GEOMETRY_FACTORY;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/4/19 21:48
 * @since:
 **/
public class DirectedDuplicateRouterTest {
    @Test
    public void test() throws Exception {
        String filePath = "E:\\1-JUST\\6-common\\1-data\\nantong\\nantong_rn_20220324.shp";
        SimpleFeatureCollection simpleFeatureCollection = GeoReader.ReadShapefile(filePath);
        RoadGraphProperty routeProperty = new RoadGraphProperty();
        long startBuildTime = System.currentTimeMillis();
        Router router = new DirectedDuplicateRouter(simpleFeatureCollection, null, routeProperty);
        System.out.println("build graph cost: " + (System.currentTimeMillis() - startBuildTime));

        long startFindNearestTime = System.currentTimeMillis();
        Node start = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.1372008, 32.3064821)));
        Node end = router.getNearestGraphNode(GEOMETRY_FACTORY.createPoint(new CoordinateXY(121.1338570, 32.3065655)));
        System.out.println("find nearest cost: " + (System.currentTimeMillis() - startFindNearestTime));

        long startAStarTime = System.currentTimeMillis();
        Path aPath = router.searchRouteAStar(start, end);
        System.out.println("astar cost: " + (System.currentTimeMillis() - startAStarTime));
        WKTWriter2 wktWriter2 = new WKTWriter2();
        for (Object edge : aPath.getEdges()) {
            System.out.println(wktWriter2.write((Geometry) ((SimpleFeature) ((Edge) edge).getObject()).getDefaultGeometry()));
        }
    }
}
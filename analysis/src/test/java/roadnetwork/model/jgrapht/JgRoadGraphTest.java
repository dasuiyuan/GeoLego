package roadnetwork.model.jgrapht;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.jgrapht.GraphPath;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Point;
import org.sylab.geolego.index.utils.GeometryUtils;
import org.sylab.geolego.io.helper.GeoReader;
import roadnetwork.RoadGraphProperty;
import roadnetwork.Router;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/5/6 11:46
 * @since:
 **/
public class JgRoadGraphTest {
    @Test
    public void buildTest() throws IOException {
        String filePath = "E:\\1-JUST\\6-common\\1-data\\beijing\\roadnetwork\\subrn.shp";
        SimpleFeatureCollection simpleFeatureCollection = GeoReader.ReadShapefile(filePath);
        RoadGraphProperty routeProperty = new RoadGraphProperty();
        long startBuildTime = System.currentTimeMillis();
        JgRoadGraph jgRoadGraph = new JgRoadGraph(simpleFeatureCollection, routeProperty);
        System.out.println(jgRoadGraph.printGraph());
        System.out.println("build graph cost: " + (System.currentTimeMillis() - startBuildTime));
    }
}
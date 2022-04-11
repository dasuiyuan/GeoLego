package roadnetwork;

import lombok.extern.slf4j.Slf4j;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.DirectedLineStringGraphGenerator;
import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.structure.DirectedGraph;
import org.geotools.graph.structure.Graph;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.Feature;

import java.util.List;

/**
 * @author Sui Yuan
 * @description
 * @date 2020/12/12 12:52
 */
@Slf4j
public class DirectedRouter extends Router {

    DirectedGraph directedGraph;

    public DirectedRouter(SimpleFeatureCollection featureCollection, List<Polygon> barriers, RouteProperty routeProperty) {
        super(featureCollection, barriers, routeProperty);
    }

    @Override
    protected Graph buildGraph(SimpleFeatureCollection featureCollection) {
        DirectedLineStringGraphGenerator gg = new DirectedLineStringGraphGenerator();
        FeatureGraphGenerator featureGraphGenerator = new FeatureGraphGenerator(gg);
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
}

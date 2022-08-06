package roadnetwork;

import lombok.extern.slf4j.Slf4j;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.DirectedLineStringGraphGenerator;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Graphable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

import java.util.*;

/**
 * @author Sui Yuan
 * @description
 * @date 2020/12/12 12:52
 */
@Slf4j
public class DirectedDuplicateRouter extends Router {

    public DirectedDuplicateRouter(SimpleFeatureCollection featureCollection, List<Polygon> barriers, RoadGraphProperty routeProperty) {
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
                //add reverse feature
                SimpleFeature reverseFeature = getReverseFeature(feature);
                Object userData = ((Geometry) reverseFeature.getDefaultGeometry()).getUserData();
                Graphable edge = featureGraphGenerator.add(reverseFeature);
                ((Geometry) ((SimpleFeature) edge.getObject()).getDefaultGeometry())
                        .setUserData(userData);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return featureGraphGenerator.getGraph();
    }

    /**
     * 获取反向要素
     * @param feature
     * @return
     */
    private SimpleFeature getReverseFeature(Feature feature) {
        SimpleFeature simpleFeature = (SimpleFeature) feature;
        SimpleFeature reverseFeature = new SimpleFeatureImpl(simpleFeature.getAttributes().toArray(), simpleFeature.getFeatureType(), simpleFeature.getIdentifier(), false);
        Geometry oriGeom = (Geometry) simpleFeature.getDefaultGeometry();
        LineString oriLineString = (LineString) oriGeom.getGeometryN(0);
        List<Coordinate> oriCoodsReverse = new ArrayList<>(Arrays.asList(oriLineString.getCoordinates()));
        Collections.reverse(oriCoodsReverse);
        Geometry reverseGeom = oriLineString.getFactory().createLineString(oriCoodsReverse.toArray(new Coordinate[0]));
        reverseFeature.setDefaultGeometry(reverseGeom);
        ((Geometry) reverseFeature.getDefaultGeometry()).setUserData(true);
        return reverseFeature;
    }
}

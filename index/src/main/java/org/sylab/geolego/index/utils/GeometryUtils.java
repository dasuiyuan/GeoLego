package org.sylab.geolego.index.utils;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.GeoJSON;
import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.geometry.jts.WKTWriter2;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Geometry;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/5/4 1:27
 * @since:
 **/
public class GeometryUtils {
    public static final WKTWriter2 WKT_WRITER_2 = new WKTWriter2();
    public static final WKTReader2 WKT_READER_2 = new WKTReader2();
    public static final GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();

    public static void main(String[] args) throws IOException {
        Point point = GEOMETRY_FACTORY.createPoint(new CoordinateXY(112.4, 23.5));
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        //要素类型名称
        typeBuilder.setName("Location");
        typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
        //构建schema
        typeBuilder.add("the_geom", Geometry.class);
        typeBuilder.add("name", String.class);
        typeBuilder.add("address", String.class);
        SimpleFeatureType simpleFeatureType = typeBuilder.buildFeatureType();
        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(simpleFeatureType);
        SimpleFeature feature = featureBuilder.buildFeature("1");
        //构建geometry和属性
        feature.setDefaultGeometry(point);
        feature.setAttribute("name", "京东大厦");
        feature.setAttribute("address", "科创十一街");
        features.add(feature);
        FeatureCollection featureCollection = new ListFeatureCollection(simpleFeatureType, features);
        StringWriter w = new StringWriter();
        GeoJSON.write(featureCollection, w);
        System.out.println(w.toString());
    }
}

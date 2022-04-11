package org.sylab.geolego.io.helper;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.sylab.geolego.model.Storage.GeoFeature;
import org.sylab.geolego.model.Storage.GeoFeatureClass;
import org.sylab.geolego.model.Storage.GeoField;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.*;

public class GeoReader {

    public final static GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();

    /**
     * 读取CSV
     *
     * @param filePath csv文件路径
     * @return
     */
    public static GeoFeatureClass ReadCSV(String filePath, String separator) {

        GeoFeatureClass geoFeatureClass = null;
        try {


            FileReader fileReader = new FileReader(filePath);
            BufferedReader reader = new BufferedReader(fileReader);
            WKTReader wktReader = new WKTReader();

            Integer index = 0;
            String line = reader.readLine();
            Map<Integer, GeoField> fieldMap = geoFieldMap(line);
            geoFeatureClass = new GeoFeatureClass((List<GeoField>) fieldMap.values());

            //读取每行记录
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(separator);
                Map<String, Object> valueMap = new HashMap<>();
                Geometry geometry = null;

                //解析每行记录
                for (String value : data) {

                    GeoField fld = fieldMap.get(index);
                    if (fld.is_isGeometry()) {
                        //convert wkt to geometry
                        try {
                            geometry = wktReader.read(value);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else if (fld.getType() == Double.class) {
                        valueMap.put(fld.getName(), Double.parseDouble(value));
                    } else if (fld.getType() == Integer.class) {
                        valueMap.put(fld.getName(), Integer.parseInt(value));
                    }

                }
                GeoFeature feature = new GeoFeature(valueMap, geometry);
                geoFeatureClass.addFeature(feature);

                index += 1;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return geoFeatureClass;
    }

    /**
     * 根据csv header构造字段映射表
     *
     * @param fieldStr
     * @return
     */
    private static Map<Integer, GeoField> geoFieldMap(String fieldStr) {
        Map<Integer, GeoField> fieldMap = new HashMap<>();
        String[] fieldArray = fieldStr.split(",");

        Integer index = 0;
        for (String field : fieldArray) {

            String fieldName = "";
            Type fieldType = String.class;
            int leftIdx = field.indexOf('(');
            int rightIdx = field.indexOf(')');
            fieldName = field.substring(0, leftIdx);
            String typeStr = field.substring(leftIdx + 1, rightIdx);
            boolean ispk = false;
            boolean isGeomtry = false;
            switch (typeStr) {
                case "int":
                    fieldType = Integer.class;
                    break;
                case "double":
                    fieldType = Double.class;
                    break;
                case "string":
                    fieldType = String.class;
                    break;
                case "shape":
                    fieldType = String.class;
                    break;
                case "INT":
                    fieldType = Integer.class;
                    ispk = true;
                    break;
                case "DOUBLE":
                    fieldType = Double.class;
                    ispk = true;
                    break;
                case "STRING":
                    fieldType = String.class;
                    ispk = true;
                    break;
                default:
                    break;
            }
            GeoField fld = new GeoField(index, fieldName, fieldType);
            fld.set_isPK(ispk);
            fld.set_isGeometry(isGeomtry);
            fieldMap.put(index, fld);
        }

        return fieldMap;
    }

    /**
     * 读取WKT文件
     *
     * @param filePath
     * @return
     */
    public static List<Geometry> ReadWKT(String filePath) {

        List<Geometry> geometryList = null;
        WKTReader wktReader = new WKTReader();
        WKTFileReader s = new WKTFileReader(filePath, wktReader);
        try {
            geometryList = s.read();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return geometryList;
    }

    /**
     * 读取shpfile文件
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static List<Geometry> ReadShp(String filePath) throws IOException {

        List<Geometry> geometryList = new ArrayList<>();
        SimpleFeatureCollection featureCollection = ReadShapefile(filePath);
        try (SimpleFeatureIterator iterator = featureCollection.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                geometry.setUserData(feature.getID());
                geometryList.add(geometry);
            }
        }
        return geometryList;

    }

    /**
     * 读geojson文件
     *
     * @param object
     * @return
     */
    public static List<SimpleFeature> readGeojson(String object) throws IOException {
        FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(15));
        FeatureCollection featureCollection = featureJSON.readFeatureCollection(object);
        SimpleFeatureIterator simpleFeatureIterator = (SimpleFeatureIterator) featureCollection.features();
        List<SimpleFeature> features = new LinkedList<>();
        while (simpleFeatureIterator.hasNext()) {
            features.add(simpleFeatureIterator.next());
        }
        return features;
    }

    /**
     * 读取shapefile文件
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static SimpleFeatureCollection ReadShapefile(String filePath) throws IOException {
        File file = new File(filePath);
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        Charset charset = ((ShapefileDataStore) dataStore).getCharset();
        ((ShapefileDataStore) dataStore).setCharset(Charset.forName("UTF-8"));
        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;
        return source.getFeatures(filter);
    }

    private static final int NUMBER_OF_DIMENSIONS = 2;
    private static final int SRID = 0;

}

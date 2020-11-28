package org.sylab.geolego.io.helper;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoTransfer {

    /**
     * 简单CSV（只包含空间信息）转为shp
     *
     * @param geomClass
     * @return
     * @throws IOException
     * @throws SchemaException
     */
    public static Boolean SimpleCSV2Shp(String inPath, String outPath, Class geomClass, Boolean isRectangle) throws IOException, SchemaException {

        File csvFile = new File(inPath);

        final SimpleFeatureType TYPE =
                DataUtilities.createType(
                        "Location",
                        "the_geom:Point:srid=4326,"
                                + // <- the geometry attribute: Point type
                                "name:String,"
                                + // <- a String attribute
                                "number:Integer" // a number attribute
                );


        /**
         * 创建要素类型，定义要素类的结构
         */
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();

        //要素类型名称
        typeBuilder.setName("Location");

        //空间字段
        typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
        typeBuilder.add("the_geom", geomClass);
        typeBuilder.add("name", String.class);

        final SimpleFeatureType GEOTYPE = typeBuilder.buildFeatureType();

        System.out.println("TYPE:" + GEOTYPE);


        //创建Feature
        List<SimpleFeature> features = new ArrayList<>();


        /**
         *  读取CSV
         */
        GeometryFactory geomFac = JTSFactoryFinder.getGeometryFactory();

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(GEOTYPE);

        try {

            FileReader fileReade = new FileReader(csvFile);
            BufferedReader bufferedReader = new BufferedReader(fileReade);

            String line = "";

            //获取头文件
            line = bufferedReader.readLine();
            System.out.println("Header：" + line);


            while ((line = bufferedReader.readLine()) != null) {

                Geometry geom = null;


                if (line.trim().length() > 0) {


                    List<Coordinate> coordinates = GetCSVCoord(line);

                    if (coordinates == null || coordinates.size() == 0) {
                        continue;
                    }

                    if (isRectangle) {
                        Coordinate leftlow = new Coordinate(coordinates.get(0).x, coordinates.get(0).y);
                        Coordinate leftup = new Coordinate(coordinates.get(0).x, coordinates.get(1).y);
                        Coordinate rightup = new Coordinate(coordinates.get(1).x, coordinates.get(1).y);
                        Coordinate rightlow = new Coordinate(coordinates.get(1).x, coordinates.get(0).y);

                        Coordinate[] recCoords = new Coordinate[]{leftlow, leftup, rightup, rightlow, leftlow};

                        geom = geomFac.createPolygon(recCoords);

                    } else if (geomClass == Point.class) {

                        geom = geomFac.createPoint(coordinates.get(0));

                    } else if (geomClass == LineString.class) {

                        geom = geomFac.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));

                    } else if (geomClass == Polygon.class) {

                        geom = geomFac.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
                    } else {

                    }

                    //此处顺序必须和TYPE创建时字段顺序一致
                    featureBuilder.add(geom);


                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    features.add(feature);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        /**
         *   创建shapfile
         */
        File newFile = new File(outPath);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        //会创建shp文件
        // The FeatureSource is used to read features, the subclass FeatureStore is used for read/write access.
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);


        newDataStore.createSchema(GEOTYPE);


        /**
         * Features 写入 shapefile
         */
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

        // The way to tell if a File can be written to in GeoTools is to use an instanceof check.
        if (featureSource instanceof SimpleFeatureStore) {


            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;


            SimpleFeatureCollection collection = new ListFeatureCollection(GEOTYPE, features);
            featureStore.setTransaction(transaction);

            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception ex) {
                ex.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }
        }

        return true;
    }


    /**
     * wkt文件转shp
     *
     * @param inPath
     * @param outPath
     * @param geomClass
     * @return
     * @throws IOException
     */
    public static Boolean SimpleWKT2Shp(String inPath, String outPath, Class geomClass) throws IOException {

        File wktFile = new File(inPath);


        //创建FeatureType

        /*final SimpleFeatureType GEOTYPE = DataUtilities.createType(
                "Location",
                "the_geom:Point:srid=4326,"
                        + // <- the geometry attribute: Point type
                        "name:String,"
                        + // <- a String attribute
                        "number:Integer" // a number attribute
        );*/

        /**
         * 创建要素类型，定义要素类的结构
         */
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();

        //要素类型名称
        typeBuilder.setName("Location");

        //空间字段
        typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
        typeBuilder.add("the_geom", geomClass);

        final SimpleFeatureType GEOTYPE = typeBuilder.buildFeatureType();

        System.out.println("TYPE:" + GEOTYPE);


        //创建Feature
        List<SimpleFeature> features = new ArrayList<>();


        /**
         *  读取WKT
         */
        GeometryFactory geomFac = JTSFactoryFinder.getGeometryFactory();

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(GEOTYPE);

        try {
            WKBWriter r = new WKBWriter();

            WKTReader wktReader = new WKTReader();
            WKTFileReader s = new WKTFileReader(inPath, wktReader);
            List<Geometry> geometryList = s.read();


            geometryList.forEach(g -> {

                if (!g.isEmpty()) {
                    featureBuilder.add(g);
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    features.add(feature);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        /**
         *   创建shapfile
         */
        File newFile = new File(outPath);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        //会创建shp文件
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

        newDataStore.createSchema(GEOTYPE);


        /**
         * Features 写入 shapefile
         */
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {

            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;


            SimpleFeatureCollection collection = new ListFeatureCollection(GEOTYPE, features);
            featureStore.setTransaction(transaction);

            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception ex) {
                ex.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }

        }
        return true;
    }



    private static List<Coordinate> GetCSVCoord(String coordLine) {

        if (coordLine.isEmpty()) {
            return null;
        }

        List<Coordinate> coordinates = new ArrayList<>();

        String[] coordStrs = coordLine.split(",");

        try {

            for (int i = 0; i < coordStrs.length; i += 2) {

                Coordinate cd = new Coordinate(Double.parseDouble(coordStrs[i]), Double.parseDouble(coordStrs[i + 1]));
                coordinates.add(cd);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return coordinates;
    }

}

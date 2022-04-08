package org.sylab.geolego.io.helper;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoWriter {

    /**
     * 将几何列表写入WKT文件，包含属性信息
     *
     * @param geometries
     * @param outPath
     * @return
     */
    public static Boolean WriteWKT(List<Geometry> geometries, String outPath) {

        FileWriter writer = null;
        BufferedWriter bufferedWriter = null;
        try {
            writer = new FileWriter(outPath);

            bufferedWriter = new BufferedWriter(writer);

            BufferedWriter finalBufferedWriter = bufferedWriter;
            geometries.forEach(g -> {
                try {
                    if (g.getUserData() != null) {
                        finalBufferedWriter.write(g.getUserData() + "&" + g.toText());
                    } else
                        finalBufferedWriter.write(g.toText());
                    finalBufferedWriter.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            finalBufferedWriter.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                writer.close();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * 写入CSV文件
     *
     * @param features
     * @param csvPath
     * @return
     */
    public static boolean writeCSV(List<String> features, String csvPath) {
        File file = new File(csvPath);
        try (FileOutputStream outputStream = new FileOutputStream(file);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {

            for (String feature : features) {
                bufferedOutputStream.write(feature.getBytes());
                bufferedOutputStream.write("\n".getBytes());
            }
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean writePureShpFile(Geometry geometry, Class geom_class, String outPath) {
        List<Geometry> geometries = new ArrayList<>();
        geometries.add(geometry);
        return writePureShpFile(geometries, geom_class, outPath);
    }

    /**
     * 将几何对象集合写入shpfile
     *
     * @param geometries
     * @param geom_class
     * @param outPath
     * @return
     */
    public static boolean writePureShpFile(List<Geometry> geometries, Class geom_class, String outPath) {
        File newFile = new File(outPath);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        ShapefileDataStore newDataStore = null;
        Map<String, Serializable> params = new HashMap<>();

        try {
            //设置shapefile数据源
            params.put("url", newFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);


            //构造SimpleFeatureType
            SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
            typeBuilder.setName("location"); //要素类型名称
            typeBuilder.setCRS(DefaultGeographicCRS.WGS84); //空间字段
            typeBuilder.add("the_geom", geom_class);
            SimpleFeatureType simpleFeatureType = typeBuilder.buildFeatureType();

            //创建要素类结构
            newDataStore.createSchema(simpleFeatureType);


            Transaction transaction = new DefaultTransaction("create");
            String typeName = newDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);


            SimpleFeatureStore simpleFeatureStore = (SimpleFeatureStore) featureSource;
            SimpleFeatureCollection simpleFeatureCollection = new ListFeatureCollection(simpleFeatureType);


            SimpleFeatureBuilder simpleFeatureBuilder = new SimpleFeatureBuilder(simpleFeatureType);
            int i = 0;
            for (Geometry g : geometries) {
                simpleFeatureBuilder.add(g);
                SimpleFeature simpleFeature = simpleFeatureBuilder.buildFeature(null);
                ((ListFeatureCollection) simpleFeatureCollection).add(simpleFeature);
            }

            simpleFeatureStore.addFeatures(simpleFeatureCollection);
            simpleFeatureStore.setTransaction(transaction);
            transaction.commit();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 将要素集合写入shpfile内
     *
     * @param featureCollection
     * @param outPath
     * @return
     */
    public static boolean writeShpfile(SimpleFeatureCollection featureCollection, String outPath) {

        File newFile = new File(outPath);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        ShapefileDataStore newDataStore = null;
        Map<String, Serializable> params = new HashMap<>();
        try {
            params.put("url", newFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            newDataStore.createSchema((SimpleFeatureType) featureCollection.getSchema());


            Transaction transaction = new DefaultTransaction("create");
            String typeName = newDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

            if (featureSource instanceof SimpleFeatureStore) {

                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

                featureStore.setTransaction(transaction);

                try {
                    featureStore.addFeatures(featureCollection);
                    transaction.commit();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    transaction.rollback();
                } finally {
                    transaction.close();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

}

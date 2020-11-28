import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.index.geohash.GeoHash;
import org.sylab.geolego.index.rtree.RTreeIndexOper;
import org.sylab.geolego.io.helper.GeoReader;

import java.io.*;
import java.util.*;

/**
 * @author : suiyuan
 * @description :
 * @date : Created in 2019-06-24 11:04
 * @modified by :
 **/
public class geohashtest {
    public static void main(String[] args) throws IOException {
        getAllGeohashData();

//        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
//        Point point = factory.createPoint(new Coordinate(1, 2));
//
//        GeoHash geoHash = new GeoHash("wx4dvwb");
////        String code = geoHash.getGeoHashCode(116.248283, 40.222012);
//
//        Envelope envelope = geoHash.getGeoPolygon();
//        System.out.println(envelope);
    }

    public static RTreeIndexOper joinNantong() throws IOException {
        RTreeIndexOper rTreeIndexOper = new RTreeIndexOper();

        //todo: 读取nantong范围
//        File file = new File("E:\\1-JUST\\2-gis\\1-data\\nantong\\南通区县geojson字符串（简化版）");
//        FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(15));
        SimpleFeatureCollection featureCollection = GeoReader.ReadShapefile("E:\\1-JUST\\2-gis\\1-data\\nantong\\xzqh-bak\\ntxzqh.shp");
        SimpleFeatureIterator iterator = featureCollection.features();
        while (iterator.hasNext()) {
            SimpleFeature feature = iterator.next();
            String name = feature.getAttribute("NAME").toString();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            geometry.setUserData(name);
            rTreeIndexOper.add(geometry);
        }
        rTreeIndexOper.buildIndex();

        return rTreeIndexOper;
    }

    public static List<GeoHash> getAllGeohashData() throws IOException {
        RTreeIndexOper rTreeIndexOper = joinNantong();
        List<GeoHash> allGeoHash7 = new ArrayList<>();
        List<String> geohash_name = new ArrayList<>();
        Stack<GeoHash> geoHashStack = new Stack<>();
        geoHashStack.push(new GeoHash("wtv3"));
        geoHashStack.push(new GeoHash("wtv2"));
        geoHashStack.push(new GeoHash("wtv9"));
        geoHashStack.push(new GeoHash("wtv8"));
        geoHashStack.push(new GeoHash("wtvc"));
        geoHashStack.push(new GeoHash("wtvb"));
        geoHashStack.push(new GeoHash("wty1"));
        geoHashStack.push(new GeoHash("wty0"));
        geoHashStack.push(new GeoHash("wty3"));
        geoHashStack.push(new GeoHash("wty2"));
        geoHashStack.push(new GeoHash("wtwr"));
        geoHashStack.push(new GeoHash("wtwp"));
        geoHashStack.push(new GeoHash("wttz"));
        geoHashStack.push(new GeoHash("wttx"));
        geoHashStack.push(new GeoHash("wttr"));
        geoHashStack.push(new GeoHash("wttq"));
        geoHashStack.push(new GeoHash("wttw"));
        geoHashStack.push(new GeoHash("wtty"));
        geoHashStack.push(new GeoHash("wtwn"));
        geoHashStack.push(new GeoHash("wtwq"));
        geoHashStack.push(new GeoHash("wtww"));
        geoHashStack.push(new GeoHash("wtwt"));
        geoHashStack.push(new GeoHash("wtwm"));
        geoHashStack.push(new GeoHash("wtwj"));
        geoHashStack.push(new GeoHash("wttv"));
        geoHashStack.push(new GeoHash("wttt"));
        geoHashStack.push(new GeoHash("wtwh"));
        geoHashStack.push(new GeoHash("wttm"));
        geoHashStack.push(new GeoHash("wtwk"));
        geoHashStack.push(new GeoHash("wtws"));
        geoHashStack.push(new GeoHash("wtw7"));
        geoHashStack.push(new GeoHash("wtwe"));
        geoHashStack.push(new GeoHash("wtvd"));


        while (!geoHashStack.isEmpty()) {
            GeoHash item = geoHashStack.pop();
            if (item.getCode().length() < 6) {
                for (GeoHash geoHash : item.getSubGeoHashLst()) {
                    geoHashStack.push(geoHash);
                }
            } else {
                for (GeoHash geoHash : item.getSubGeoHashLst()) {
                    List<Geometry> geometries = rTreeIndexOper.searchIntersect(geoHash.getExtent(), false);
                    if (geometries == null || geometries.size() == 0) {
                        continue;
                    } else {
                        for (Geometry geometry : geometries) {
                            geohash_name.add(geoHash.getCode() + "," + geometry.getUserData());
                        }
                    }
                }
            }
        }

        try (FileWriter writer = new FileWriter("E:\\1-JUST\\2-gis\\1-data\\nantong\\geohash\\geohash_name.csv"); BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            int count = 0;
            for (String s : geohash_name) {
                count++;
                bufferedWriter.write(s);
                bufferedWriter.newLine();
                if (count == 10000) {
                    bufferedWriter.flush();
                    count = 0;
                }
            }
        } catch (Exception ex) {

        }

        return null;
    }
}

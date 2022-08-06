package org.sylab.geolego.io.helper;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;
import org.opengis.feature.simple.SimpleFeature;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class GeoReaderTest {

    @Test
    public void readShapefile() throws IOException {
        GeoReader.ReadShapefile("/E:/1-JUST/3-DE/3-code/just-de/just-de-algorithm/local-algorithm/target/classes/data/shp/beijing_rn_sub.shp");

        Set<String> roadNameSet = new HashSet<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("E:\\work\\files\\JUST\\GIS\\data\\南京-矢量-utm\\road_dit.txt"));
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("E:\\work\\files\\JUST\\GIS\\data\\南京-矢量-utm\\road.csv"))) {
            String layerName;
            while ((layerName = bufferedReader.readLine()) != null) {
                roadNameSet.add(layerName);
            }

            SimpleFeatureCollection featureCollection = GeoReader.ReadShapefile("E:\\work\\files\\JUST\\GIS\\data\\南京-矢量-utm\\线.shp");
            SimpleFeatureIterator iterator = featureCollection.features();
            WKTWriter wktWriter = new WKTWriter();
            List<String> featureList = new ArrayList<>();
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                String layer = feature.getAttribute("LAYER").toString();
                if (!roadNameSet.contains(layer)) {
                    continue;
                }
                String wkt = wktWriter.write((Geometry) feature.getDefaultGeometry());
                featureList.add(layer + "&" + wkt);
            }

            for (String s : featureList) {
                bufferedWriter.write(s);
                bufferedWriter.newLine();
            }
        }

    }
}
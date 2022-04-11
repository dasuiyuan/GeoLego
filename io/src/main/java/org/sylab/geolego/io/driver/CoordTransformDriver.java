package org.sylab.geolego.io.driver;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.sylab.geolego.model.utils.CoordTransformUtils;

import java.io.*;

/**
 * @author Sui Yuan
 * @description
 * @date 2021/2/4 16:32
 */
public class CoordTransformDriver {
    public static void main(String[] args) throws FileNotFoundException {
        WKTReader2 reader2 = new WKTReader2();
        WKTWriter2 writer2 = new WKTWriter2();
        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();

        File source = new File("E:\\1-JUST\\2-gis\\1-data\\nantong\\poi.csv");
        File target = new File("E:\\1-JUST\\2-gis\\1-data\\nantong\\poi_84.csv");
        try (BufferedReader reader = new BufferedReader(new FileReader(source)); BufferedWriter writer = new BufferedWriter(new FileWriter(target))) {
            String poi = reader.readLine();
            int i = 0;
            while (poi != null) {
                String[] infos = poi.split(";");
                Point pt = (Point) reader2.read(infos[0]);
                double[] coords = CoordTransformUtils.gcj02Towgs84(pt.getX(), pt.getY());
                Point pt2 = factory.createPoint(new CoordinateXY(coords[0], coords[1]));
                Geometry geo=null;

                infos[0] = writer2.write(pt2);
                String finalStr = String.join(";", infos);
//                System.out.println(finalStr);
                writer.write(finalStr);
                writer.newLine();
                poi = reader.readLine();
                if (i++ % 500 == 0) {
                    System.out.println("count: " + i);
                    writer.flush();
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}

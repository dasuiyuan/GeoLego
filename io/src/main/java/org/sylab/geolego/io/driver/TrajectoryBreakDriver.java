package org.sylab.geolego.io.driver;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.sylab.geolego.io.helper.GeoReader;
import org.sylab.geolego.model.Storage.GeoFeature;
import org.sylab.geolego.model.Storage.GeoFeatureClass;

import java.io.IOException;
import java.util.List;

/**
 * @author Sui Yuan
 * @description
 * @date 2020/12/8 18:01
 */
public class TrajectoryBreakDriver {
    public static void main(String[] args) throws IOException {
        //TODO：load all trajectories
        List<Geometry> geometries = GeoReader.ReadShp("E:\\1-JUST\\2-gis\\1-data\\nantong\\trajectory.shp");
        System.out.println(geometries.size());
        for (Geometry geometry : geometries) {
            LineString lineString = (LineString) geometry;
        }
    }
}

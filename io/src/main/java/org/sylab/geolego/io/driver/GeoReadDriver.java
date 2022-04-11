package org.sylab.geolego.io.driver;

import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.io.helper.GeoReader;
import org.sylab.geolego.io.utils.FileUtil;

import java.io.IOException;
import java.util.List;

/**
 * @author Sui Yuan
 * @description
 * @date 2020/11/30 14:52
 */
public class GeoReadDriver {
    public static void main(String[] args) throws IOException {
        String geojson = "E:\\1-JUST\\2-gis\\3-code\\just-gis1105\\just-gis-server\\src\\main\\resources\\data\\trajectory\\traj1.geojson";
        String jsonContent = FileUtil.readAll(geojson);
        List<SimpleFeature> featureList = GeoReader.readGeojson(jsonContent);
    }
}

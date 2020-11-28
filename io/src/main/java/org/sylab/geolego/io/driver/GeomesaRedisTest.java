package org.sylab.geolego.io.driver;

import org.geotools.feature.SchemaException;
import org.locationtech.geomesa.utils.geotools.SimpleFeatureTypes;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GeomesaRedisTest {
    public static void main(String[] args) throws IOException, SchemaException {

        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("redis.url", "redis://:CsAqBdHz4Ever@10.222.109.38:8108");
        parameters.put("redis.catalog", "realtime_catalog");
        org.geotools.data.DataStore dataStore =
                org.geotools.data.DataStoreFinder.getDataStore(parameters);
        SimpleFeatureType featureType = SimpleFeatureTypes.createType("realtime", "name:String,trajectory:String,*geom:Point:srid=4326");

        dataStore.createSchema(featureType);
    }
}

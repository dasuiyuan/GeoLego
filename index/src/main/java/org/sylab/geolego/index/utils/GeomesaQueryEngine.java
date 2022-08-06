package org.sylab.geolego.index.utils;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author : suiyuan
 * @description : Geomesa查询引擎
 * @date : Created in 2019-10-11 12:42
 * @modified by :
 **/
public class GeomesaQueryEngine {

    /**
     * 基本查询方法
     *
     * @param sft     表名
     * @param cql     查询条件cql
     * @return simple feature列表
     * @throws IOException  IOException
     * @throws CQLException CQLException
     */
    public static List<SimpleFeature> QueryFeatures(String sft, String cql) throws IOException, CQLException {
        DataStore dataStore = DataStoreCacheHelper.getInstance().getDataStore();
        if (dataStore == null) {
            return null;
        }
        Query query = new Query(sft, ECQL.toFilter(cql));
        List<SimpleFeature> simpleFeatures = new LinkedList<>();
        try (FeatureReader<SimpleFeatureType, SimpleFeature> reader = dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT)) {
            while (reader.hasNext()) {
                simpleFeatures.add(reader.next());
            }
        }
        return simpleFeatures;
    }
}

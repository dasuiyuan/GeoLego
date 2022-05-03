package org.sylab.geolego.index.utils;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * @Author: Sui Yuan
 * @Description:
 * @Date: 2022/5/4 1:27
 * @since:
 **/
public class GeometryUtils {
    public static final WKTWriter2 WKT_WRITER_2 = new WKTWriter2();
    public static final WKTReader2 WKT_READER_2 = new WKTReader2();
    public static final GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();
}

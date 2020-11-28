package driver;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.renderer.lite.RendererUtilities;
import org.locationtech.jts.geom.*;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.sylab.geolego.io.helper.GeoReader;
import org.sylab.geolego.io.helper.GeoWriter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : suiyuan
 * @description :
 * @date : Created in 2020-05-09 11:55
 * @modified by :
 **/
public class World2ScreenDriver {
    public static void main(String[] args) throws TransformException, IOException {
        List<Geometry> geometries = GeoReader.ReadShp("E:\\work\\files\\JUST\\GIS\\data\\XZQ84\\province_Project.shp");
        List<Geometry> screenGeometry = new ArrayList<>(geometries.size());
//        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
//        Coordinate[] coordinates = new Coordinate[5];
//        coordinates[0] = new CoordinateXY(0, 0);
//        coordinates[1] = new CoordinateXY(1, 1);
//        coordinates[2] = new CoordinateXY(1, 2);
//        coordinates[3] = new CoordinateXY(2, 4);
//        coordinates[4] = new CoordinateXY(3, 4);
//        LineString lineString = geometryFactory.createLineString(coordinates);

        ReferencedEnvelope envelope = new ReferencedEnvelope(new Envelope(0, 180, -90, 90), DefaultGeographicCRS.WGS84);
        AffineTransform affineTransform = RendererUtilities.worldToScreenTransform(envelope, new Rectangle(4096, 4096));
        MathTransform mathTransform = ProjectiveTransform.create(affineTransform);
//        Geometry screen = JTS.transform(lineString, mathTransform);

        for (Geometry geometry : geometries) {
            Geometry screen = JTS.transform(geometry, mathTransform);
            screenGeometry.add(screen);
            break;
        }
        GeoWriter.WriteWKT(screenGeometry, "E:\\work\\files\\JUST\\GIS\\data\\tilegrid\\province_screen_tile_env.wkt");
    }
}

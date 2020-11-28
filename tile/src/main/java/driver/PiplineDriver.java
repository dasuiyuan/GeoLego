package driver;

import com.wdtinc.mapbox_vector_tile.adapt.jts.MvtEncoder;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsLayer;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsMvt;
import helper.Pipeline;
import helper.PipelineBuilder;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.sylab.geolego.io.helper.GeoReader;
import org.sylab.geolego.io.helper.GeoWriter;
import util.FileUtils;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;


/**
 * @author : suiyuan
 * @description :
 * @date : Created in 2020-05-07 15:32
 * @modified by :
 **/
public class PiplineDriver {

    public static void main(String[] args) throws FactoryException, IOException {
        List<Geometry> geometries = GeoReader.ReadShp("E:\\work\\files\\JUST\\GIS\\data\\XZQ84\\province_Project.shp");
        CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:4326");
        double overSamplingFactor = 2.0;
        boolean clipToMapBounds = true;
        boolean transformToScreenCoordinates = true;

        final ReferencedEnvelope renderingArea = new ReferencedEnvelope(new Envelope(114, 118.2, 39, 42.2), CRS.decode("EPSG:4326"));
        Rectangle paintArea = new Rectangle(4096, 4096);

        PipelineBuilder builder =
                PipelineBuilder.newBuilder(
                        renderingArea, paintArea, sourceCrs, overSamplingFactor, 0);

        Pipeline pipeline =
                builder.preprocess()
                        .transform(transformToScreenCoordinates)
                        .clip(clipToMapBounds, transformToScreenCoordinates)
                        .simplify(transformToScreenCoordinates)
                        .collapseCollections()
                        .build();
        List<Geometry> geometryList = runPip(pipeline, geometries);


//        //这里看即使geometry转换正常，pbf之后也有问题，所以不能用JtsLayer
        JtsLayer layer = new JtsLayer("province",geometryList, 4096);
        JtsMvt mvt = new JtsMvt(singletonList(layer));
        byte[] encoded = MvtEncoder.encode(mvt);
        FileUtils.write2File("E:\\work\\files\\JUST\\GIS\\data\\XZQ84\\prov_pipline\\beijing_6.pbf", encoded);
//
//        //尝试用vectorTileEncoder

//        GeoWriter.WriteWKT(geometryList, "E:\\work\\files\\JUST\\GIS\\data\\XZQ84\\prov_pipline\\beijing_6.wkt");
    }

    private static List<Geometry> runPip(Pipeline pipeline, List<Geometry> geometryList) {
        List<Geometry> geometries = new ArrayList<>(geometryList.size());
        for (Geometry originalGeom : geometryList) {
            try {
                Geometry finalGeom = pipeline.execute(originalGeom);
                if (!finalGeom.isEmpty()) {
                    geometries.add(finalGeom);
                }
            } catch (Exception processingException) {
                processingException.printStackTrace();
                continue;
            }
        }
        return geometries;
    }
}
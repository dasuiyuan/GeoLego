package driver;

import com.wdtinc.mapbox_vector_tile.adapt.jts.MvtEncoder;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsLayer;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsMvt;
import helper.Pipeline;
import helper.PipelineBuilder;
import model.TileManager;
import model.TilePiece;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.sylab.geolego.io.helper.GeoReader;
import org.sylab.geolego.io.helper.GeoWriter;
import util.FileUtils;
import util.SpatialUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * @author : suiyuan
 * @description :
 * @date : Created in 2020-05-09 09:29
 * @modified by :
 **/
public class DynamicScaleVectorTileDriver {
    public static void main(String[] args) throws IOException, FactoryException {
        createTileByGeometries("E:\\work\\files\\JUST\\GIS\\data\\XZQ84\\province_Project.shp", "E:\\work\\files\\JUST\\GIS\\data\\tile\\dynamic");
    }

    private static void createTileByGeometries(String shpfile, String outFolder) throws IOException, FactoryException {
        CoordinateReferenceSystem sourceCrs = DefaultGeographicCRS.WGS84;
        double overSamplingFactor = 2.0;
        boolean clipToMapBounds = true;
        boolean transformToScreenCoordinates = true;

        List<Geometry> geometries = GeoReader.ReadShp(shpfile);
        Envelope env = new Envelope(73, 135, 18, 55);
        TileManager tileManager = new TileManager(4096);
        tileManager.setJustLoadGeom(true);
        Map<Integer, List<TilePiece>> tilePieces = tileManager.createTiles(0, 6, env, geometries);

        if (tilePieces != null) {

            for (Map.Entry<Integer, List<TilePiece>> entry : tilePieces.entrySet()) {
                int level = entry.getKey();
                for (TilePiece tilePiece : entry.getValue()) {

                    File colDir = new File(outFolder + File.separator + level + File.separator + tilePiece.getColNum());
                    colDir.mkdirs();
                    String pbf = colDir.getPath() + File.separator + tilePiece.getRowNum() + ".pbf";
                    byte[] encoded = null;
                    if (tilePiece.getGeometries() != null) {

                        final ReferencedEnvelope renderingArea = new ReferencedEnvelope(tilePiece.getMbr(), DefaultGeographicCRS.WGS84);
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
                        List<Geometry> finalGeomtries = runPip(pipeline, geometries);

                        JtsLayer layer = new JtsLayer("layer", finalGeomtries, 4096);
                        JtsMvt mvt = new JtsMvt(singletonList(layer));
                        encoded = MvtEncoder.encode(mvt);

                    }
                    FileUtils.write2File(pbf, encoded);
                }
            }
        }
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

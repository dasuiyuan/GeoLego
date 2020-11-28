/* (c) 2015 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package lite;

import com.google.common.base.Preconditions;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.util.factory.Hints;
import org.geotools.util.logging.Logging;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;

import java.util.logging.Logger;

/**
 * Utility methods to deal with transformations and style based queries.
 *
 * <p>Note, most code in this class has been taken and adapted from GeoTools' StreamingRenderer.
 */
public class VectorMapRenderUtils {

    private static final Logger LOGGER = Logging.getLogger(VectorMapRenderUtils.class);

    private static final FilterFactory2 FF = CommonFactoryFinder.getFilterFactory2();

    /**
     * Builds the transform from sourceCRS to destCRS/
     *
     * <p>Although we ask for 2D content (via {@link Hints#FEATURE_2D} ) not all DataStore
     * implementations are capable. With that in mind if the provided soruceCRS is not 2D we are
     * going to manually post-process the Geomtries into {@link DefaultGeographicCRS#WGS84} - and
     * the {@link MathTransform2D} returned here will transition from WGS84 to the requested
     * destCRS.
     *
     * @return the transform from {@code sourceCRS} to {@code destCRS}, will be an identity
     * transform if the the two crs are equal
     * @throws FactoryException If no transform is available to the destCRS
     */
    public static MathTransform buildTransform(
            CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem destCRS)
            throws FactoryException {
        Preconditions.checkNotNull(sourceCRS, "sourceCRS");
        Preconditions.checkNotNull(destCRS, "destCRS");

        MathTransform transform = null;
        if (sourceCRS.getCoordinateSystem().getDimension() >= 3) {
            // We are going to transform over to DefaultGeographic.WGS84 on the fly
            // so we will set up our math transform to take it from there
            MathTransform toWgs84_3d =
                    CRS.findMathTransform(sourceCRS, DefaultGeographicCRS.WGS84_3D);
            MathTransform toWgs84_2d =
                    CRS.findMathTransform(
                            DefaultGeographicCRS.WGS84_3D, DefaultGeographicCRS.WGS84);
            transform = ConcatenatedTransform.create(toWgs84_3d, toWgs84_2d);
            sourceCRS = DefaultGeographicCRS.WGS84;
        }

        // the basic crs transformation, if any
        MathTransform2D sourceToTarget;
        sourceToTarget = (MathTransform2D) CRS.findMathTransform(sourceCRS, destCRS, true);

        //3D Src -> 2D WGS84 转换为空
        if (transform == null) {
            return sourceToTarget;
        }
        //src -> tag 没有任何改变
        if (sourceToTarget.isIdentity()) {
            return transform;
        }
        //返回 3D Src -> 2D WGS84 -> tag
        return ConcatenatedTransform.create(transform, sourceToTarget);
    }
}

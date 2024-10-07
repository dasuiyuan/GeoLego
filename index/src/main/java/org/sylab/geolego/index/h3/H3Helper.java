package org.sylab.geolego.index.h3;

import com.uber.h3core.H3Core;

import java.io.IOException;

public class H3Helper {

    private static H3Core h3Core;

    static {
        try {
            h3Core = H3Core.newInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeAddress(double lat, double lng, int res) throws IOException {
//        double lat = 37.775938728915946;
//        double lng = -122.41795063018799;
//        int res = 9;
        return h3Core.latLngToCellAddress(lat, lng, res);
    }

    public static long encode(double lat, double lng, int res) throws IOException {
        return h3Core.latLngToCell(lat, lng, res);
    }
}

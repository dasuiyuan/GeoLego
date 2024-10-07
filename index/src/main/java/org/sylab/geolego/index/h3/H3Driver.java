package org.sylab.geolego.index.h3;

import java.io.IOException;

public class H3Driver {

    public static void main(String[] args) throws IOException {
        System.out.println(H3Helper.encode(32.55, 116.32, 8));
        System.out.println(H3Helper.encodeAddress(32.55, 116.32, 8));
    }
}

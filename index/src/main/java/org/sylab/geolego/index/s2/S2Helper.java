package org.sylab.geolego.index.s2;

import com.google.common.geometry.*;

import java.util.ArrayList;
import java.util.List;

public class S2Helper {

    public static S2CellId point2CellId(double lng, double lat) {
        //注意使用的是WGS84坐标（GPS导航坐标）
        //parent()可以指定等级，默认是30级
        lat = 36.683;
        lng = 117.1412;
        int currentlevel = 4;
        S2LatLng s2LatLng = S2LatLng.fromDegrees(lat, lng);
        S2CellId cellId = S2CellId.fromLatLng(s2LatLng).parent(currentlevel);

        //CellID(face=1, pos=15d0000000000000, level=4)
        System.out.println("CellID" + cellId);

        //CellID.pos:1571756269952303104
        System.out.println("CellID.pos:" + cellId.pos());

        //CellID.id: 3877599279165997056,level:4
        System.out.println("CellID.id: " + cellId.id() + ",level:" + cellId.level());
        return cellId;
    }

    public static double datumDistance(double lng1, double lat1, double lng2, double lat2) {
        S2LatLng startS2 = S2LatLng.fromDegrees(lat1, lng1);
        S2LatLng endS2 = S2LatLng.fromDegrees(lng2, lng2);
        double distance = startS2.getEarthDistance(endS2);
        System.out.println("距离为：" + distance + " m");
        return distance;
    }

    public static List<S2CellId> getRegionCellIds(S2Polygon polygon, int maxLevel, int minLevel, int maxCells) {
        List<S2CellId> regionCellIds = new ArrayList<S2CellId>();
        //S2Region cap 任意区域
        S2RegionCoverer coverer = new S2RegionCoverer();
        //最小格子和最大格子，总格子数量
        coverer.setMaxLevel(maxLevel);
        coverer.setMinLevel(minLevel);
        coverer.setMaxCells(maxCells);
        List<S2CellId> list = coverer.getCovering(polygon).cellIds();
        for (S2CellId s : list) {
            regionCellIds.add(s);
        }
        //可以用于区域内目标检索，根据cellid建立索引,查询区域内cellid in （list）的餐馆、出租车
        return regionCellIds;
    }
}

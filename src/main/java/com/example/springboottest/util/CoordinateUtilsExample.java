package com.example.springboottest.util;

/**
 * 经纬度转换工具使用示例
 */
public class CoordinateUtilsExample {

    public static void main(String[] args) {
        // 北京天安门坐标示例（WGS84）
        double longitude = 116.397128;
        double latitude = 39.916527;

        System.out.println("========== 坐标转换示例 ==========");
        System.out.println("原始坐标（WGS84-GPS坐标）: " + longitude + ", " + latitude);

        // WGS84 转 GCJ02
        CoordinateUtils.Coordinate gcj02 = CoordinateUtils.wgs84ToGcj02(longitude, latitude);
        System.out.println("转换为 GCJ02（火星坐标）: " + gcj02);

        // WGS84 转 BD09
        CoordinateUtils.Coordinate bd09 = CoordinateUtils.wgs84ToBd09(longitude, latitude);
        System.out.println("转换为 BD09（百度坐标）: " + bd09);

        // GCJ02 转 WGS84
        CoordinateUtils.Coordinate wgs84Back = CoordinateUtils.gcj02ToWgs84(
                gcj02.getLongitude(), gcj02.getLatitude()
        );
        System.out.println("GCJ02 转回 WGS84: " + wgs84Back);

        // GCJ02 转 BD09
        CoordinateUtils.Coordinate bd09FromGcj = CoordinateUtils.gcj02ToBd09(
                gcj02.getLongitude(), gcj02.getLatitude()
        );
        System.out.println("GCJ02 转 BD09: " + bd09FromGcj);

        // BD09 转 GCJ02
        CoordinateUtils.Coordinate gcj02Back = CoordinateUtils.bd09ToGcj02(
                bd09.getLongitude(), bd09.getLatitude()
        );
        System.out.println("BD09 转 GCJ02: " + gcj02Back);

        System.out.println("\n========== 度分秒转换示例 ==========");
        // 十进制度数转度分秒
        CoordinateUtils.DMS dms = CoordinateUtils.decimalToDMS(longitude);
        System.out.println("经度 " + longitude + " 转换为度分秒: " + dms);

        dms = CoordinateUtils.decimalToDMS(latitude);
        System.out.println("纬度 " + latitude + " 转换为度分秒: " + dms);

        // 度分秒转十进制度数
        double decimal = CoordinateUtils.dmsToDecimal(116, 23, 49.6608);
        System.out.println("度分秒 116°23′49.66″ 转换为十进制: " + decimal);

        System.out.println("\n========== 距离计算示例 ==========");
        // 北京天安门和上海外滩的距离
        double bjLon = 116.397128;
        double bjLat = 39.916527;
        double shLon = 121.490317;
        double shLat = 31.240526;

        double distance = CoordinateUtils.calculateDistance(bjLon, bjLat, shLon, shLat);
        System.out.println("北京天安门到上海外滩的直线距离: " + String.format("%.2f", distance) + " 千米");

        // 使用Coordinate对象计算距离
        CoordinateUtils.Coordinate beijing = new CoordinateUtils.Coordinate(bjLon, bjLat);
        CoordinateUtils.Coordinate shanghai = new CoordinateUtils.Coordinate(shLon, shLat);
        distance = CoordinateUtils.calculateDistance(beijing, shanghai);
        System.out.println("使用Coordinate对象计算距离: " + String.format("%.2f", distance) + " 千米");

        System.out.println("\n========== 坐标验证示例 ==========");
        System.out.println("经度 " + longitude + " 是否合法: " + CoordinateUtils.isValidLongitude(longitude));
        System.out.println("纬度 " + latitude + " 是否合法: " + CoordinateUtils.isValidLatitude(latitude));
        System.out.println("坐标 (" + longitude + ", " + latitude + ") 是否合法: " +
                CoordinateUtils.isValidCoordinate(longitude, latitude));

        // 测试非法坐标
        System.out.println("经度 200 是否合法: " + CoordinateUtils.isValidLongitude(200));
        System.out.println("纬度 -100 是否合法: " + CoordinateUtils.isValidLatitude(-100));
    }
}

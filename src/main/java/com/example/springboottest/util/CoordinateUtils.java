package com.example.springboottest.util;

/**
 * 经纬度转换工具类
 * 支持 WGS84（GPS）、GCJ02（国测局/火星坐标系）、BD09（百度坐标系）之间的相互转换
 * 以及度分秒格式转换和距离计算
 */
public final class CoordinateUtils {

    private CoordinateUtils() {
        // 私有构造函数，防止实例化
    }

    // 常量定义
    private static final double X_PI = 3.14159265358979324 * 3000.0 / 180.0;
    private static final double PI = 3.1415926535897932384626;
    private static final double A = 6378245.0; // 长半轴
    private static final double EE = 0.00669342162296594323; // 偏心率平方
    private static final double EARTH_RADIUS = 6371.0; // 地球平均半径（单位：千米）

    /**
     * 坐标点类
     */
    public static class Coordinate {
        private double longitude; // 经度
        private double latitude;  // 纬度

        public Coordinate(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        @Override
        public String toString() {
            return "Coordinate{" +
                    "longitude=" + longitude +
                    ", latitude=" + latitude +
                    '}';
        }
    }

    /**
     * 度分秒格式类
     */
    public static class DMS {
        private int degrees;  // 度
        private int minutes;  // 分
        private double seconds; // 秒

        public DMS(int degrees, int minutes, double seconds) {
            this.degrees = degrees;
            this.minutes = minutes;
            this.seconds = seconds;
        }

        public int getDegrees() {
            return degrees;
        }

        public int getMinutes() {
            return minutes;
        }

        public double getSeconds() {
            return seconds;
        }

        @Override
        public String toString() {
            return degrees + "°" + minutes + "′" + String.format("%.2f", seconds) + "″";
        }
    }

    // ==================== WGS84 与 GCJ02 转换 ====================

    /**
     * WGS84 转 GCJ02（GPS坐标 转 火星坐标）
     *
     * @param longitude WGS84经度
     * @param latitude  WGS84纬度
     * @return GCJ02坐标
     */
    public static Coordinate wgs84ToGcj02(double longitude, double latitude) {
        if (outOfChina(longitude, latitude)) {
            return new Coordinate(longitude, latitude);
        }
        double dLat = transformLat(longitude - 105.0, latitude - 35.0);
        double dLon = transformLon(longitude - 105.0, latitude - 35.0);
        double radLat = latitude / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);
        double mgLat = latitude + dLat;
        double mgLon = longitude + dLon;
        return new Coordinate(mgLon, mgLat);
    }

    /**
     * GCJ02 转 WGS84（火星坐标 转 GPS坐标）
     * 使用二分法进行精确转换
     *
     * @param longitude GCJ02经度
     * @param latitude  GCJ02纬度
     * @return WGS84坐标
     */
    public static Coordinate gcj02ToWgs84(double longitude, double latitude) {
        if (outOfChina(longitude, latitude)) {
            return new Coordinate(longitude, latitude);
        }
        double initDelta = 0.01;
        double threshold = 0.000000001;
        double dLat = initDelta;
        double dLon = initDelta;
        double mLat = latitude - dLat;
        double mLon = longitude - dLon;
        double pLat = latitude + dLat;
        double pLon = longitude + dLon;
        double wgsLat, wgsLon;
        int i = 0;
        while (true) {
            wgsLat = (mLat + pLat) / 2;
            wgsLon = (mLon + pLon) / 2;
            Coordinate tmp = wgs84ToGcj02(wgsLon, wgsLat);
            dLat = tmp.getLatitude() - latitude;
            dLon = tmp.getLongitude() - longitude;
            if ((Math.abs(dLat) < threshold) && (Math.abs(dLon) < threshold)) {
                break;
            }
            if (dLat > 0) {
                pLat = wgsLat;
            } else {
                mLat = wgsLat;
            }
            if (dLon > 0) {
                pLon = wgsLon;
            } else {
                mLon = wgsLon;
            }
            if (++i > 10000) {
                break;
            }
        }
        return new Coordinate(wgsLon, wgsLat);
    }

    // ==================== GCJ02 与 BD09 转换 ====================

    /**
     * GCJ02 转 BD09（火星坐标 转 百度坐标）
     *
     * @param longitude GCJ02经度
     * @param latitude  GCJ02纬度
     * @return BD09坐标
     */
    public static Coordinate gcj02ToBd09(double longitude, double latitude) {
        double z = Math.sqrt(longitude * longitude + latitude * latitude) + 0.00002 * Math.sin(latitude * X_PI);
        double theta = Math.atan2(latitude, longitude) + 0.000003 * Math.cos(longitude * X_PI);
        double bdLon = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        return new Coordinate(bdLon, bdLat);
    }

    /**
     * BD09 转 GCJ02（百度坐标 转 火星坐标）
     *
     * @param longitude BD09经度
     * @param latitude  BD09纬度
     * @return GCJ02坐标
     */
    public static Coordinate bd09ToGcj02(double longitude, double latitude) {
        double x = longitude - 0.0065;
        double y = latitude - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        double gcjLon = z * Math.cos(theta);
        double gcjLat = z * Math.sin(theta);
        return new Coordinate(gcjLon, gcjLat);
    }

    // ==================== WGS84 与 BD09 转换 ====================

    /**
     * WGS84 转 BD09（GPS坐标 转 百度坐标）
     *
     * @param longitude WGS84经度
     * @param latitude  WGS84纬度
     * @return BD09坐标
     */
    public static Coordinate wgs84ToBd09(double longitude, double latitude) {
        Coordinate gcj02 = wgs84ToGcj02(longitude, latitude);
        return gcj02ToBd09(gcj02.getLongitude(), gcj02.getLatitude());
    }

    /**
     * BD09 转 WGS84（百度坐标 转 GPS坐标）
     *
     * @param longitude BD09经度
     * @param latitude  BD09纬度
     * @return WGS84坐标
     */
    public static Coordinate bd09ToWgs84(double longitude, double latitude) {
        Coordinate gcj02 = bd09ToGcj02(longitude, latitude);
        return gcj02ToWgs84(gcj02.getLongitude(), gcj02.getLatitude());
    }

    // ==================== 度分秒转换 ====================

    /**
     * 十进制度数转度分秒格式
     *
     * @param decimal 十进制度数
     * @return 度分秒对象
     */
    public static DMS decimalToDMS(double decimal) {
        int degrees = (int) decimal;
        double minutesDecimal = (decimal - degrees) * 60;
        int minutes = (int) minutesDecimal;
        double seconds = (minutesDecimal - minutes) * 60;
        return new DMS(degrees, minutes, seconds);
    }

    /**
     * 度分秒格式转十进制度数
     *
     * @param degrees 度
     * @param minutes 分
     * @param seconds 秒
     * @return 十进制度数
     */
    public static double dmsToDecimal(int degrees, int minutes, double seconds) {
        return degrees + minutes / 60.0 + seconds / 3600.0;
    }

    // ==================== 距离计算 ====================

    /**
     * 计算两个坐标点之间的距离（使用Haversine公式）
     *
     * @param lon1 第一个点的经度
     * @param lat1 第一个点的纬度
     * @param lon2 第二个点的经度
     * @param lat2 第二个点的纬度
     * @return 距离（单位：千米）
     */
    public static double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 计算两个坐标点之间的距离
     *
     * @param coord1 第一个坐标点
     * @param coord2 第二个坐标点
     * @return 距离（单位：千米）
     */
    public static double calculateDistance(Coordinate coord1, Coordinate coord2) {
        return calculateDistance(
                coord1.getLongitude(), coord1.getLatitude(),
                coord2.getLongitude(), coord2.getLatitude()
        );
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断坐标是否在中国境外
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return true表示在中国境外
     */
    private static boolean outOfChina(double longitude, double latitude) {
        return longitude < 72.004 || longitude > 137.8347 ||
                latitude < 0.8293 || latitude > 55.8271;
    }

    /**
     * 纬度转换辅助函数
     */
    private static double transformLat(double longitude, double latitude) {
        double ret = -100.0 + 2.0 * longitude + 3.0 * latitude + 0.2 * latitude * latitude +
                0.1 * longitude * latitude + 0.2 * Math.sqrt(Math.abs(longitude));
        ret += (20.0 * Math.sin(6.0 * longitude * PI) + 20.0 * Math.sin(2.0 * longitude * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(latitude * PI) + 40.0 * Math.sin(latitude / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(latitude / 12.0 * PI) + 320 * Math.sin(latitude * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 经度转换辅助函数
     */
    private static double transformLon(double longitude, double latitude) {
        double ret = 300.0 + longitude + 2.0 * latitude + 0.1 * longitude * longitude +
                0.1 * longitude * latitude + 0.1 * Math.sqrt(Math.abs(longitude));
        ret += (20.0 * Math.sin(6.0 * longitude * PI) + 20.0 * Math.sin(2.0 * longitude * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(longitude * PI) + 40.0 * Math.sin(longitude / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(longitude / 12.0 * PI) + 300.0 * Math.sin(longitude / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    // ==================== 坐标验证 ====================

    /**
     * 验证经度是否合法
     *
     * @param longitude 经度
     * @return true表示合法
     */
    public static boolean isValidLongitude(double longitude) {
        return longitude >= -180 && longitude <= 180;
    }

    /**
     * 验证纬度是否合法
     *
     * @param latitude 纬度
     * @return true表示合法
     */
    public static boolean isValidLatitude(double latitude) {
        return latitude >= -90 && latitude <= 90;
    }

    /**
     * 验证坐标是否合法
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return true表示合法
     */
    public static boolean isValidCoordinate(double longitude, double latitude) {
        return isValidLongitude(longitude) && isValidLatitude(latitude);
    }
}

package demo.order.util;

public class GeoUtils {

    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff = Math.toRadians(lon2 - lon1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) *
                Math.cos(latitude2) * Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    public static double[] findPointAtDistanceFrom(double[] startPoint, double initialBearingRadians, double distanceKilometres) {
        double radiusEarthKilometres = 6371.01;
        var distRatio = distanceKilometres / radiusEarthKilometres;
        var distRatioSine = Math.sin(distRatio);
        var distRatioCosine = Math.cos(distRatio);

        var startLatRad = Math.toRadians(startPoint[0]);
        var startLonRad = Math.toRadians(startPoint[1]);

        var startLatCos = Math.cos(startLatRad);
        var startLatSin = Math.sin(startLatRad);

        var endLatRads = Math.asin((startLatSin * distRatioCosine) + (startLatCos * distRatioSine * Math.cos(initialBearingRadians)));

        var endLonRads = startLonRad + Math.atan2(Math.sin(initialBearingRadians) * distRatioSine * startLatCos,
                distRatioCosine - startLatSin * Math.sin(endLatRads));

        return new double[]{Math.toDegrees(endLatRads), Math.toDegrees(endLonRads)};
    }
}

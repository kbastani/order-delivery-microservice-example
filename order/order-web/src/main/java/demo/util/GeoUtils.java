package demo.util;

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

    public static double distance(double lat1, double lon1, double lat2, double lon2, DistanceUnit unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals(DistanceUnit.KILOMETERS)) {
                dist = dist * 1.609344;
            } else if (unit.equals(DistanceUnit.NAUTICAL_MILES)) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }
}

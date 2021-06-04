package demo.order.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GeoUtilsTest {

    @Test
    public void isBearingEqualTest() {
        double radius = 1.0;
        double actual = GeoUtils.bearing(26.26421356201172, -81.8227767944336, 26.450441360473633, -81.7912826538086);

        Assertions.assertEquals(8.609791970756987, actual);
    }

    @Test
    public void isBearingEndPointEqualTest() {
        double bearing = 8.609791970756987;
        double[] actual = GeoUtils.findPointAtDistanceFrom(new double[]{26.26421356201172, -81.8227767944336},
                Math.toRadians(bearing), 5.0);

        double[] expected = new double[]{26.30867264594247, -81.81526735670498};
        Assertions.assertArrayEquals(expected, actual);
    }

}
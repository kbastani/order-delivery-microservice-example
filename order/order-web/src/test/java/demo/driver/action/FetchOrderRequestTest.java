package demo.driver.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.driver.domain.NearbyPreparedOrder;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FetchOrderRequestTest {

    @Test
    public void testNearbyOrderSerialization() {
        NearbyPreparedOrder[] nearbyOrders = {};
        List<Map<String, Object>> orders = null;
        ObjectMapper objectMapper = new ObjectMapper();

        orders = IntStream.range(0, 5).mapToObj(i -> {
            Map<String, Object> row = new HashMap<>();
            row.put("orderId", i);
            row.put("preparedAt", DateTime.now().toDateTime().getMillis() - 10000L);
            row.put("preparedAge", 10000L);
            row.put("distance", 1400.0);
            return row;
        }).collect(Collectors.toList());

        try {
            nearbyOrders = objectMapper.readValue(objectMapper.writeValueAsString(orders), NearbyPreparedOrder[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Matchers.equalTo(nearbyOrders.length).matches(5);
    }
}
package demo.order.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceClientTest {
    @Autowired
    private OrderServiceClient orderServiceClient;

    @Test
    void orderClientGetRequestFailsWithRetry() {
        orderServiceClient.get(1L);
    }
}
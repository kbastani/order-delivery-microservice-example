package demo;

import demo.config.PinotJdbcTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles("test")
public class OrderWebApplicationTests {

	@Test
	public void contextLoads() {
	}

}

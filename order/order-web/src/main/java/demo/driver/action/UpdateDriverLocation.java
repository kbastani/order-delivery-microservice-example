package demo.driver.action;

import demo.domain.Action;
import demo.driver.domain.Driver;
import demo.driver.domain.DriverService;
import demo.driver.event.DriverEvent;
import demo.driver.event.DriverEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Connects an {@link Driver} to an Account.
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class UpdateDriverLocation extends Action<Driver> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DriverService driverService;

    public UpdateDriverLocation(DriverService driverService) {
        this.driverService = driverService;
    }

    public Driver apply(Driver driver, Double lat, Double lon) {
        driver.setLat(lat);
        driver.setLon(lon);
        driver = driverService.update(driver);

        try {
            driver.appendEvent(new DriverEvent(DriverEventType.LOCATION_UPDATED, driver));
        } catch (Exception ex) {
            log.error("Could not update driver location", ex);
        }

        return driver;
    }
}

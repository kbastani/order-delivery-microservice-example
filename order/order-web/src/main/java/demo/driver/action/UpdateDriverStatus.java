package demo.driver.action;

import demo.domain.Action;
import demo.driver.domain.Driver;
import demo.driver.domain.DriverService;
import demo.driver.domain.DriverStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Updates the status of a {@link Driver} entity.
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class UpdateDriverStatus extends Action<Driver> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DriverService driverService;

    public UpdateDriverStatus(DriverService driverService) {
        this.driverService = driverService;
    }

    public Driver apply(Driver driver, DriverStatus driverStatus) {
        // Save rollback status
        DriverStatus rollbackStatus = driver.getDriverStatus();

        try {
            // Update status
            driver.setDriverStatus(driverStatus);
            driver = driverService.update(driver);
        } catch (Exception ex) {
            log.error("Could not update the status", ex);
            driver.setDriverStatus(rollbackStatus);
            driver = driverService.update(driver);
        }

        return driver;
    }
}

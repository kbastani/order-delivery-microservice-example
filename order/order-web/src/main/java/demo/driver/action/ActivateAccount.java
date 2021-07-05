package demo.driver.action;

import demo.domain.Action;
import demo.driver.domain.Driver;
import demo.driver.domain.DriverActivityStatus;
import demo.driver.domain.DriverService;
import demo.driver.domain.DriverStatus;
import demo.driver.event.DriverEvent;
import demo.driver.event.DriverEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Connects an {@link Driver} to an Account.
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class ActivateAccount extends Action<Driver> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DriverService driverService;

    public ActivateAccount(DriverService driverService) {
        this.driverService = driverService;
    }

    public Driver apply(Driver driver) {
        checkDriverState(driver);

        DriverStatus previousState = driver.getDriverStatus();
        driver.setDriverStatus(DriverStatus.DRIVER_ACTIVE);
        driver.setEventType(DriverEventType.ACCOUNT_ACTIVATED);
        driver.setActivityStatus(DriverActivityStatus.DRIVER_WAITING);
        driver = driverService.update(driver);

        try {
            driver.appendEvent(new DriverEvent(DriverEventType.ACCOUNT_ACTIVATED, driver));
        } catch (Exception ex) {
            log.error("Could not update driver state", ex);
            driver.setDriverStatus(previousState);
            driver.setActivityStatus(DriverActivityStatus.ACCOUNT_PENDING);
            driver = driverService.update(driver);
        }

        return driver;
    }

    private void checkDriverState(Driver driver) {
        try {
            Assert.isTrue(driver.getDriverStatus() == DriverStatus.DRIVER_CREATED ||
                            driver.getDriverStatus() == DriverStatus.DRIVER_SUSPENDED,
                    String.format("Driver must be in a DRIVER_CREATED or DRIVER_SUSPENDED state. {state=%s}",
                            driver.getDriverStatus()));
        } catch (Exception ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

}

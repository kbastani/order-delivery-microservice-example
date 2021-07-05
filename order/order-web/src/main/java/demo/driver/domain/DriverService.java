package demo.driver.domain;

import demo.domain.Service;
import demo.driver.event.DriverEvent;
import demo.driver.event.DriverEventType;
import demo.driver.repository.DriverRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@org.springframework.stereotype.Service
@Transactional(timeout = 6000)
public class DriverService extends Service<Driver, Long> {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Driver registerDriver(Driver driver) {
        driver = create(driver);
        driver.appendEvent(new DriverEvent(DriverEventType.DRIVER_CREATED, driver));
        return driver;
    }

    /**
     * Create a new {@link Driver} entity.
     *
     * @param driver is the {@link Driver} to create
     * @return the newly created {@link Driver}
     */
    public Driver create(Driver driver) {

        // Save the driver to the repository
        driver = driverRepository.save(driver);

        return driver;
    }

    /**
     * Get an {@link Driver} entity for the supplied identifier.
     *
     * @param id is the unique identifier of a {@link Driver} entity
     * @return an {@link Driver} entity
     */
    public Driver get(Long id) {
        return driverRepository.findById(id).orElse(null);
    }

    /**
     * Update an {@link Driver} entity with the supplied identifier.
     *
     * @param driver is the {@link Driver} containing updated fields
     * @return the updated {@link Driver} entity
     */
    @Transactional
    public Driver update(Driver driver) {
        Assert.notNull(driver.getIdentity(), "Driver id must be present in the resource URL");
        Assert.notNull(driver, "Driver request body cannot be null");

        Assert.state(driverRepository.existsById(driver.getIdentity()),
                "The driver with the supplied id does not exist");

        Driver currentDriver = get(driver.getIdentity());
        currentDriver.setDriverStatus(driver.getDriverStatus());
        currentDriver.setActivityStatus(driver.getActivityStatus());
        currentDriver.setAvailabilityStatus(driver.getAvailabilityStatus());
        currentDriver.setLat(driver.getLat());
        currentDriver.setLon(driver.getLon());

        return driverRepository.save(currentDriver);
    }

    /**
     * Delete the {@link Driver} with the supplied identifier.
     *
     * @param id is the unique identifier for the {@link Driver}
     */
    public boolean delete(Long id) {
        Assert.state(driverRepository.existsById(id),
                "The driver with the supplied id does not exist");
        this.driverRepository.deleteById(id);
        return true;
    }
}

package scheduler;

/**
 * A scheduler takes in an order that describes the delivery of resources. The scheduler is used by multiple
 * vendors to schedule deliveries in any chronological order.
 */
public class Scheduler {
    private Track track;

    public Scheduler(Track track) {
        this.track = track;
    }
}

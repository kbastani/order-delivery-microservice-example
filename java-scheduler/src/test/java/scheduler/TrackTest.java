package scheduler;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrackTest {
    @Test
    public void blockingTrackDeliveryTest() throws Exception {
        SignalRepository signalRepository = new SignalRepository();

        Cart<Integer>[] carts = new Cart[]{};
        ExpandingResource<Cart<Integer>, Integer> resource =
                new ExpandingResource<>(carts, Cart::new, signalRepository);

        Track<Integer> track = new Track<>(resource);

        // Creates a single-threaded track that delivers four orderRequests over two seconds
        List<OrderRequest<Integer>> orderRequests = IntStream.of(1, 2, 3, 4)
                .mapToObj(k -> {
                    Resource<Integer> r = Resource.of(k);
                    return new OrderRequest<>((long) r.hashCode(), k * 5L, r);
                }).collect(Collectors.toList());

        orderRequests.forEach(track::schedule);

        while (!track.isEmpty()) {
            System.out.println(Arrays.toString(track.deliver().stream().flatMap(Collection::stream)
                    .collect(Collectors.toList()).toArray(Integer[]::new)));
            Thread.sleep(100);
        }
    }

    @Test
    public void asyncTrackDeliveryTest() throws Exception {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        SignalRepository signalRepository = new SignalRepository();

        Cart<Integer>[] carts = new Cart[]{};
        ExpandingResource<Cart<Integer>, Integer> resource =
                new ExpandingResource<>(carts, Cart::new, signalRepository);

        Track<Integer> track = new Track<>(resource);

        // Creates a single-threaded track that delivers four orderRequests over two seconds
        List<OrderRequest<Integer>> orderRequests = IntStream.of(1, 2, 3, 4)
                .mapToObj(k -> {
                    Resource<Integer> r = Resource.of(k);
                    return new OrderRequest<>((long) r.hashCode(), (long) k, r);
                }).collect(Collectors.toList());

        orderRequests.forEach(track::schedule);

        while (!track.isEmpty()) {
            long frame = Math.round(track.deliver().stream().flatMap(Collection::stream).findFirst().orElse(0));
            executor.schedule(() -> System.out.println(frame), frame * 500, TimeUnit.MILLISECONDS);
        }

        Thread.sleep(2500);
    }

    static class SignalRepository implements StreamingRepository<Cart<Integer>, Integer> {

        private final Map<Long, Cart<Integer>> repo = new HashMap<>();

        @Override
        public Cart<Integer> getById(Long id) {
            return repo.get(id);
        }

        @Override
        public void save(Cart<Integer> item) {
            repo.put(item.getId(), item);
        }

        @Override
        public OrderRequest<Integer> saveOrder(OrderRequest<Integer> orderRequest) {
            return orderRequest;
        }

        @Override
        public void remove(Long id) {
            repo.remove(id);
        }

        @Override
        public boolean isEmpty() {
            return repo.isEmpty();
        }
    }
}
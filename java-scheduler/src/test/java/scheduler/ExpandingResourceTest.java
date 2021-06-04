package scheduler;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class ExpandingResourceTest {
    @Test
    @SuppressWarnings("unchecked")
    public void take() throws Exception {
        TestStreamingRepository repository = new TestStreamingRepository();

        LongStream.range(10L, 20L)
                .forEach(i -> {
                    Cart<Integer> result = new Cart<Integer>(i);
                    ScheduledEvent<Integer> scheduledEvent = new ScheduledEvent<Integer>(i, new Resource<Integer>(4, 0, new Integer[]{0, 1, 2, 3}));
                    result.commit(scheduledEvent);
                    repository.save(result);
                });

        Cart<Integer>[] carts = new Cart[]{};
        ExpandingResource<Cart<Integer>, Integer> resource = new ExpandingResource<Cart<Integer>, Integer>(0, 0, carts, (i) -> {
            Cart<Integer> result = new Cart<Integer>(i);
            ScheduledEvent<Integer> scheduledEvent = new ScheduledEvent<>(i, new Resource<Integer>(3, 0, new Integer[]{1, 1, 1}));
            result.commit(scheduledEvent);
            return result;
        }, repository);

        LongStream.range(0L, 40L).forEach(i -> {
            //System.out.println(.deliver());
            Cart<Integer> item = (Cart<Integer>) Stream.of(resource.take(1).toArray()).findFirst().get();
            //Arrays.toString(result[0].deliver().toArray(Integer[]::new));
            String result = item.streamMultiChannel().map(x -> Arrays.toString((x.toArray()))).collect(Collectors.joining(","));
            System.out.println(item.getId() + ": " + result);
        });

    }

    class TestStreamingRepository implements StreamingRepository<Cart<Integer>, Integer> {

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
        public ScheduledEvent<Integer> saveOrder(ScheduledEvent<Integer> scheduledEvent) {
            return scheduledEvent;
        }

        @Override
        public void remove(Long id) {
            repo.remove(id);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
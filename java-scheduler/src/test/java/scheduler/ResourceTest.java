package scheduler;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class ResourceTest {
    @Test
    public void takeFiveAndIncrementPosition() throws Exception {

        Resource<Integer> resource = new Resource<>(10, 0, IntStream.range(0, 10).boxed()
                .toArray(Integer[]::new));
        Integer[] actual = new Integer[]{0, 1, 2, 3, 4};
        Integer actualPos = 5;

        Assert.assertArrayEquals(resource.take(5).toArray(), actual);
        Assert.assertEquals(resource.getPosition(), actualPos);
    }

    @Test
    public void asyncIncrementalExhaustion() throws Exception {
        Resource<Integer> resource = new Resource<>(10, 0, IntStream.range(0, 10).boxed()
                .toArray(Integer[]::new));
        ExecutorService executor = Executors.newFixedThreadPool(10);

        final ConcurrentLinkedQueue<Integer> buffer = new ConcurrentLinkedQueue<>();
        List<Callable<Boolean>> invocations = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            invocations.add(() -> buffer.offer(resource.take(1).stream().findFirst().orElse(-1)));
        }

        invocations.forEach(executor::submit);
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);

        Integer[] actual = buffer.stream().sorted().toArray(Integer[]::new);
        Integer[] expected = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        Assert.assertArrayEquals(expected, actual);
        Assert.assertEquals(resource.getState(), ResourceState.EXHAUSTED);
    }
}
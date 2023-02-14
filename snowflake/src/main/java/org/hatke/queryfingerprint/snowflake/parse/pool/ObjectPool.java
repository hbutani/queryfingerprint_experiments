package org.hatke.queryfingerprint.snowflake.parse.pool;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ObjectPool<T> {
    private final ConcurrentLinkedQueue<T> pool =  new ConcurrentLinkedQueue<>();
    private ScheduledExecutorService executorService;
    // This public constructor is necessary for (de+)serialization done by Spark
    public ObjectPool() {}

    public ObjectPool(final int minObjects, final int maxObjects, final long validationIntervalInSeconds) {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            int size = pool.size();
            if (size < minObjects) {
                int sizeToBeAdded = minObjects - size;
                initialize(sizeToBeAdded);
            } else if (size > maxObjects) {
                int sizeToBeRemoved = size - maxObjects;
                for (int i = 0; i < sizeToBeRemoved; i++) {
                    pool.poll();
                }
            }
        }, validationIntervalInSeconds, validationIntervalInSeconds, TimeUnit.SECONDS);
    }

    protected abstract T createObject();

    public T borrowObject() {
        T object;
        if ((object = pool.poll()) == null) {
            object = createObject();
        }
        return object;
    }

    public void returnObject(T object) {
        if (object == null) {
            return;
        }
        this.pool.offer(object);
    }

    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    protected void initialize(int numberOfObjects) {
        Arrays.stream(new int[numberOfObjects]).parallel().forEach(i -> pool.add(createObject()));
    }
}

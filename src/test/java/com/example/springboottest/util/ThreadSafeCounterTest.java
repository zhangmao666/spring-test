package com.example.springboottest.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadSafeCounterTest {

    @Test
    void shouldIncrementAndReturnCurrentCount() {
        ThreadSafeCounter counter = new ThreadSafeCounter();

        int first = counter.increment();
        int second = counter.increment();

        assertEquals(1, first);
        assertEquals(2, second);
        assertEquals(2, counter.getCount());
    }

    @Test
    void shouldKeepAccurateCountWhenIncrementedConcurrently() throws InterruptedException {
        ThreadSafeCounter counter = new ThreadSafeCounter();
        int threadCount = 10;
        int incrementsPerThread = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        try {
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        for (int j = 0; j < incrementsPerThread; j++) {
                            counter.increment();
                        }
                    } finally {
                        doneSignal.countDown();
                    }
                });
            }

            boolean completed = doneSignal.await(5, TimeUnit.SECONDS);
            assertTrue(completed, "并发任务未在预期时间内完成");
            assertEquals(threadCount * incrementsPerThread, counter.getCount());
        } finally {
            executorService.shutdownNow();
        }
    }
}

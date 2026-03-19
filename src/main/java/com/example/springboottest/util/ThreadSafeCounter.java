package com.example.springboottest.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于 AtomicInteger 的线程安全计数器。
 */
public class ThreadSafeCounter {

    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 计数加一并返回最新值。
     *
     * @return 增加后的计数值
     */
    public int increment() {
        return counter.incrementAndGet();
    }

    /**
     * 获取当前计数值。
     *
     * @return 当前计数值
     */
    public int getCount() {
        return counter.get();
    }
}

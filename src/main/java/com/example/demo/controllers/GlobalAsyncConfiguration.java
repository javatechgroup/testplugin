package com.example.demo.controllers;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

/**
 * Tests AG029 precision fix:
 * Implementing AsyncConfigurer provides a safe, bounded global thread pool.
 * Therefore, bare @Async in DemoAsyncService is safely pooled and must NOT be flagged.
 */
@Configuration
@EnableAsync
public class GlobalAsyncConfiguration implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("app-async-");
        executor.initialize();
        return executor;
    }
}

@Service
class DemoAsyncService {

    // AG029 Test: Bare @Async in an app with AsyncConfigurer -> Compliant (NOT FLAGGED)
    @Async
    public void executeBackgroundNotification() {
        System.out.println("Executing bounded background notification");
    }
}

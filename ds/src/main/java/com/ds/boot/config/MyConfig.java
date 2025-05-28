package com.ds.boot.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ds.boot.controller.TestController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ds
 * @date 2025/4/22
 * @description
 */
@ConditionalOnClass(TestController.class)
@Configuration
public class MyConfig {

	private static final int CORE_POOL_SIZE = 5;

	private static final int MAX_POOL_SIZE = 10;

	private static final int QUEUE_CAPACITY = 100;

	private static final Long KEEP_ALIVE_TIME = 1L;

	@Bean("executor")
	public Executor executor() {
		return new ThreadPoolExecutor(
				CORE_POOL_SIZE,
				MAX_POOL_SIZE,
				KEEP_ALIVE_TIME,
				TimeUnit.SECONDS,
				new ArrayBlockingQueue<>(QUEUE_CAPACITY),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}

}

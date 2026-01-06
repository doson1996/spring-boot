package com.ds.boot.task;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author ds
 * @date 2026/1/6
 * @description
 */
@Component
public class MyTask {

	@Scheduled(fixedRate = 5000)
	public void print() {
		System.out.println(LocalDateTime.now() + " " + Thread.currentThread().getName() + " | MyTask Print...");
	}

}

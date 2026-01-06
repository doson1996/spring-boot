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
	public void print1() {
		System.out.println(LocalDateTime.now() + " " + Thread.currentThread().getName() + " | MyTask Print1 Start...");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		System.out.println(LocalDateTime.now() + " " + Thread.currentThread().getName() + " | MyTask Print1 End...");
	}

	@Scheduled(fixedRate = 5000)
	public void print2() {
		System.out.println(LocalDateTime.now() + " " + Thread.currentThread().getName() + " | MyTask Print2 Start...");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		System.out.println(LocalDateTime.now() + " " + Thread.currentThread().getName() + " | MyTask Print2 End...");
	}

	@Scheduled(cron = "0 */1 * * * *")
	public void cronPrint() {
		System.out.println(LocalDateTime.now() + " " + Thread.currentThread().getName() + " | MyTask CronPrint...");
	}

}

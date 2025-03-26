package com.ds.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.Map;

/**
 * @author ds
 * @date 2024/12/25
 * @description
 */
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
		// 读取字符串配置
		String appName = context.getEnvironment().getProperty("appName");
		System.out.println("appName = " + appName);

		// 读取集合配置
		List list = context.getEnvironment().getProperty("list", List.class);
		System.out.println("list = " + list);

		// 读取map配置
		Map map = context.getEnvironment().getProperty("map", Map.class);
		System.out.println("map = " + map);
	}
}

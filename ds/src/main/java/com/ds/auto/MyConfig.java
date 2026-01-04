package com.ds.auto;

import com.ds.boot.controller.TestController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * @author ds
 * @date 2025/4/22
 * @description
 */
@ConditionalOnClass(TestController.class)
@Configuration
public class MyConfig {

	@Value("${ds.name:ds}")
	private String name;

	public MyConfig() {
		System.out.println("MyConfig...");
	}

}

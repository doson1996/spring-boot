package com.ds.boot.config;

import com.ds.boot.annotion.InjectionAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ds
 * @date 2025/4/22
 * @description
 */
@Configuration
public class MyConfig {

	@Bean
	public InjectionAnnotationBeanPostProcessor injectionAnnotationBeanPostProcessor() {
		return new InjectionAnnotationBeanPostProcessor();
	}

}

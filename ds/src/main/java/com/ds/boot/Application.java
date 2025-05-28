package com.ds.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author ds
 * @date 2024/12/25
 * @description
 * 		1.注册ProxyAsyncConfiguration @EnableAsync -> @Import(AsyncConfigurationSelector.class) -> ProxyAsyncConfiguration
 *		2.在该配置类注册AsyncAnnotationBeanPostProcessor（AsyncAnnotationBeanPostProcessor 实现了AbstractBeanFactoryAwareAdvisingPostProcessor，而AbstractBeanFactoryAwareAdvisingPostProcessor继承了AbstractAdvisingBeanPostProcessor + 实现了BeanFactoryAware）
 *		3.在AsyncAnnotationBeanPostProcessor#setBeanFactory中创建AsyncAnnotationAdvisor，实现对有@Async的方法和类进行代理
 *		4.在AsyncAnnotationAdvisor构造方法中buildAdvice创建AnnotationAsyncExecutionInterceptor，实现对有@Async的方法进行代理
 *		5.被代理的类和方法执行时会走到org.springframework.aop.interceptor.AsyncExecutionInterceptor#invoke实现异步执行流程
 *
 *		tips: 不要使用默认线程池，它会为每个任务新起一个线程 {@link org.springframework.core.task.SimpleAsyncTaskExecutor#doExecute(java.lang.Runnable)}
 */
@SpringBootApplication
@EnableAsync
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

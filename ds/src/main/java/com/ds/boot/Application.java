package com.ds.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author ds
 * @date 2024/12/25
 * @description
 * 		1.注册SchedulingConfiguration @EnableScheduling -> @Import(SchedulingConfiguration.class) -> SchedulingConfiguration
 *		2.在该配置类注册ScheduledAnnotationBeanPostProcessor（ScheduledAnnotationBeanPostProcessor 实现了MergedBeanDefinitionPostProcessor）
 *		3.在ScheduledAnnotationBeanPostProcessor#postProcessAfterInitialization中收集所有带有@Scheduled注解的方法，
 *		并且将方法封装成ScheduledMethodRunnable（ScheduledAnnotationBeanPostProcessor#processScheduled）
 *		4.在ScheduledAnnotationBeanPostProcessor#postProcessAfterInitialization中将ScheduledMethodRunnable封装成Task(如CronTask、FixedDelayTask等),
 *		并且将Task添加到scheduledTasks中
 *		5.最后通过ContextRefreshedEvent事件触发ScheduledAnnotationBeanPostProcessor#finishRegistration方法，
 *		最终执行ScheduledTaskRegistrar的afterPropertiesSet方法调用scheduleTasks方法，开始调度任务
 */
@EnableScheduling
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

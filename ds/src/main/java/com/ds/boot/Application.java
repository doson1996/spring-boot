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
 *		并且将方法封装成ScheduledMethodRunnable {@link org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor#processScheduled}
 *		4.在ScheduledAnnotationBeanPostProcessor#postProcessAfterInitialization中将ScheduledMethodRunnable封装成Task(如CronTask、FixedDelayTask等),
 *		并且将Task添加到scheduledTasks中
 *		5.最后通过ContextRefreshedEvent事件触发ScheduledAnnotationBeanPostProcessor#finishRegistration方法，
 *		最终执行ScheduledTaskRegistrar的afterPropertiesSet方法调用scheduleTasks方法，开始调度任务
 *
 *		通过EnableScheduling注解注入了SchedulingConfiguration，在此配置类中又声明了ScheduledAnnotationBeanPostProcessor,在后置方法中去扫描bean的方法有没有
 * 		Scheduled注解，如果有，将当前bean和方法封装成ScheduledMethodRunnable（以便需要时在run方法中通过反射执行当前方法），然后根据注解的值封装成对应
 * 		的task(如FixedDelayTask)添加到任务列表中，这一步就完成了任务的收集，最后监听ContextRefreshedEvent事件，调用finishRegistration->this.registrar.afterPropertiesSet()
 * 	    ->scheduleTasks() 开启对各种任务的调度
 */
@EnableScheduling
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

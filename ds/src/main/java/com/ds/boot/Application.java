package com.ds.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author ds
 * @date 2024/12/25
 * @description
 * 		1.注册InfrastructureAdvisorAutoProxyCreator、ProxyTransactionManagementConfiguration @EnableTransactionManagement -> @Import(TransactionManagementConfigurationSelector.class) -> AutoProxyRegistrar[-> InfrastructureAdvisorAutoProxyCreator]、ProxyTransactionManagementConfiguration
 *		2.
 *		3.
 *		4.
 *		5.
 *
 */
@EnableTransactionManagement
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

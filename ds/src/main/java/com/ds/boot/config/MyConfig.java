package com.ds.boot.config;

import com.ds.boot.controller.TestController;
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
}

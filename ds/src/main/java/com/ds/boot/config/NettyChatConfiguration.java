package com.ds.boot.config;

import com.ds.boot.server.NettyChatServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ds
 * @date 2025/7/23
 * @description
 */
@Configuration
public class NettyChatConfiguration {

	@Bean
	public CommandLineRunner chatServerRunner() {
		return args -> {
			// 在单独的线程中启动Netty聊天服务器
			new Thread(() -> {
				try {
					new NettyChatServer(8090).start();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					e.printStackTrace();
				}
			}).start();
		};
	}

}

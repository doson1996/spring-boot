package com.ds.boot.sse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author ds
 * @date 2026/8/25
 * @description
 */
public class SSEManager {

	// 使用 ConcurrentHashMap 保证线程安全
	private static final Map<String, SseEmitter> EMITTER_MAP = new ConcurrentHashMap<>();

	/**
	 * 新增或更新一个 SseEmitter 连接
	 * @param userId 用来标识不同用户的唯一ID
	 * @param emitter SseEmitter 实例
	 */
	public static void addEmitter(String userId, SseEmitter emitter) {
		// 如果该用户已有连接，可以先关掉旧的，再放新的
		SseEmitter oldEmitter = EMITTER_MAP.put(userId, emitter);
		if (oldEmitter != null) {
			try {
				oldEmitter.complete(); // 完成旧的连接
			} catch (Exception e) {
				// 忽略关闭时的异常
			}
		}
	}

	/**
	 * 根据 userId 发送消息
	 * @param userId 用户ID
	 * @param message 要发送的消息内容
	 */
	public static void sendMessage(String userId, String message) {
		SseEmitter emitter = EMITTER_MAP.get(userId);
		if (emitter != null) {
			try {
				// 构建并发送 SSE 数据
				emitter.send(SseEmitter.event().data(message));
			} catch (IOException e) {
				// 发送失败说明连接已断开，从管理器中移除
				removeEmitter(userId);
			}
		}
	}

	/**
	 * 移除并关闭一个连接
	 * @param userId 用户ID
	 */
	public static void removeEmitter(String userId) {
		SseEmitter emitter = EMITTER_MAP.remove(userId);
		if (emitter != null) {
			try {
				emitter.complete(); // 正常完成
			} catch (Exception e) {
				// 忽略异常
			}
		}
	}

	/**
	 * 当连接因超时或错误而完成时，自动清理资源
	 */
	public static void completeEmitter(String userId) {
		removeEmitter(userId);
	}

}

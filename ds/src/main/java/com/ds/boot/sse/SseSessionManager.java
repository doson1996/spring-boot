package com.ds.boot.sse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author 11596
 */
@Component
public class SseSessionManager {

	private static final Logger log = LoggerFactory.getLogger(SseSessionManager.class);

	/**
	 * userId -> SseClient，ConcurrentHashMap 保证注册/查询/删除的原子性
	 */
	private final Map<String, SseHolder> clients = new ConcurrentHashMap<>();

	/**
	 * 默认 5 分钟超时
	 */
	private static final long DEFAULT_TIMEOUT = 5 * 60 * 1000L;

	public SseEmitter createEmitter(String userId) {
		final SseHolder client = new SseHolder(userId, DEFAULT_TIMEOUT);
		final SseEmitter emitter = client.getEmitter();

		// 回调里只操作 map，绝不获取 client 的锁，避免与 send/complete 死锁
		// 用 remove(key, value) 两参数版本，防止误删同 userId 的新连接(ABA)
		emitter.onCompletion(() -> {
			clients.remove(userId, client);
			log.info("SSE 会话已清除: userId={}, 剩余在线={}", userId, clients.size());
		});

		emitter.onTimeout(() -> {
			log.info("SSE 连接超时: userId={}", userId);
			// complete() 会同步触发 onCompletion，由它统一清理
			client.complete();
		});

		emitter.onError(e -> {
			log.info("SSE 连接异常: userId={}, 原因={}", userId, e.toString());
			clients.remove(userId, client);
		});

		// put 是原子的，返回被替换的旧连接
		SseHolder old = clients.put(userId, client);
		if (old != null) {
			log.info("用户 {} 重复连接，关闭旧会话", userId);
			old.complete();   // 旧连接的 onCompletion 里是 remove(userId, oldClient)，不会误删新连接
		}

		return emitter;
	}

	/**
	 * 向指定用户推送，线程安全。
	 */
	public boolean sendToUser(String userId, String eventName, Object data) {
		SseHolder client = clients.get(userId);
		if (client == null) {
			return false;
		}
		boolean ok = client.send(eventName, data);
		if (!ok) {
			// 发送失败立即从注册表摘除，保证及时性（不等回调）
			clients.remove(userId, client);
			client.completeWithError(new IllegalStateException("send failed"));
		}
		return ok;
	}

	/**
	 * 广播，线程安全。ConcurrentHashMap 的迭代器是弱一致的，遍历中删除安全。
	 */
	public void broadcast(String eventName, Object data) {
		clients.values().forEach(client -> {
			if (!client.send(eventName, data)) {
				clients.remove(client.getUserId(), client);
				client.completeWithError(new IllegalStateException("broadcast failed"));
			}
		});
	}

	public int getOnlineCount() {
		return clients.size();
	}

	public List<String> onlineList() {
		return clients.keySet().stream().sorted().collect(Collectors.toList());
	}

	public boolean isOnline(String userId) {
		return clients.containsKey(userId);
	}

	/**
	 * 应用关闭时优雅断开所有连接
	 */
	@PreDestroy
	public void shutdown() {
		log.info("应用关闭，清理 {} 个 SSE 连接", clients.size());
		clients.values().forEach(SseHolder::complete);
		clients.clear();
	}

	public String disconnect(String userId) {
		SseHolder sseHolder = clients.remove(userId);
		String removeUserId = sseHolder.getUserId();
		log.info("{}断开连接", removeUserId);
		return removeUserId;
	}

	public void clear() {
		clients.clear();
	}
}

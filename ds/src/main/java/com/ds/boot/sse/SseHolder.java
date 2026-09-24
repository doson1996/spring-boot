package com.ds.boot.sse;

import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 单个 SSE 会话的线程安全包装。
 * 所有对 SseEmitter 的写操作都必须经过本类的锁。
 * @author 11596
 */
public class SseHolder {

	private static final Logger log = LoggerFactory.getLogger(SseHolder.class);

	private final String userId;
	private final SseEmitter emitter;

	/**
	 * 保护 emitter.send / complete 的互斥锁
	 */
	private final ReentrantLock lock = new ReentrantLock();

	/**
	 * 是否已关闭，用于快速短路，避免无谓加锁
	 */
	private volatile boolean closed = false;

	public SseHolder(String userId, long timeoutMillis) {
		this.userId = userId;
		this.emitter = new SseEmitter(timeoutMillis);
	}

	public String getUserId() {
		return userId;
	}

	public SseEmitter getEmitter() {
		return emitter;
	}

	public boolean isClosed() {
		return closed;
	}

	/**
	 * 线程安全地发送一条事件。
	 *
	 * @return true 表示发送成功；false 表示会话已关闭或发送失败
	 */
	public boolean send(String eventName, Object data) {
		if (closed) {
			return false;
		}
		lock.lock();
		try {
			// 双重检查：可能在等待锁期间已被其它线程关闭
			if (closed) {
				return false;
			}
			emitter.send(SseEmitter.event().name(eventName).data(data));
			return true;
		} catch (Exception e) {
			// IOException: 客户端断开(ClientAbortException)
			// IllegalStateException: emitter 已完成
			closed = true;
			log.debug("SSE 发送失败, userId={}, 原因={}", userId, e.toString());
			return false;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 线程安全地正常结束会话
	 */
	public void complete() {
		lock.lock();
		try {
			if (closed) {
				return;
			}
			closed = true;
			emitter.complete();
		} catch (Exception e) {
			log.debug("SSE complete 异常, userId={}", userId, e);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 线程安全地以异常结束会话
	 */
	public void completeWithError(Throwable t) {
		lock.lock();
		try {
			if (closed) {
				return;
			}
			closed = true;
			emitter.completeWithError(t);
		} catch (Exception e) {
			log.debug("SSE completeWithError 异常, userId={}", userId, e);
		} finally {
			lock.unlock();
		}
	}

}

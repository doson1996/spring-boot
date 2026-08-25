package com.ds.boot.controller;

import java.util.HashMap;
import java.util.Map;

import com.ds.boot.sse.SSEManager;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author ds
 * @date 2026/8/25
 * @description
 */
@RestController
@RequestMapping("/sse")
public class SseController {

	@GetMapping(path = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter connect(@RequestParam String userId) {
		// 超时时间设为较长，比如30分钟
		SseEmitter emitter = new SseEmitter(1800000L);
		// 这里可以用一个管理类把 emitter 存起来，方便后续推送
		SSEManager.addEmitter(userId, emitter);
		return emitter;
	}

	@GetMapping(path = "/send", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> connect(@RequestParam String userId, @RequestParam String message) {
		Map<String, Object> result = new HashMap<>();
		SSEManager.sendMessage(userId, message);
		result.put("code", "ok");
		return result;
	}

}

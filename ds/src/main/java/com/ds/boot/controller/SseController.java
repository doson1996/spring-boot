package com.ds.boot.controller;

import java.util.List;

import com.ds.boot.sse.SseSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
@CrossOrigin(origins = "*")
public class SseController {

	@Autowired
	private SseSessionManager sessionManager;

	@GetMapping(value = "/connect", produces = "text/event-stream;charset=UTF-8")
	public SseEmitter connect(@RequestParam("userId") String userId) {
		return sessionManager.createEmitter(userId);
	}

	@GetMapping(value = "/disconnect")
	public String disconnect(@RequestParam("userId") String userId) {
		return sessionManager.disconnect(userId);
	}

	@GetMapping(value = "/clear")
	public String clear() {
		sessionManager.clear();
		return "clear";
	}

	@GetMapping("/send")
	public String send(@RequestParam String userId,
					   @RequestParam String message) {
		boolean ok = sessionManager.sendToUser(userId, "message", message);
		return ok ? "发送成功" : "用户不在线";
	}

	@GetMapping("/broadcast")
	public String broadcast(@RequestParam String message) {
		sessionManager.broadcast("message", message);
		return "广播完成，在线数：" + sessionManager.getOnlineCount();
	}

	@GetMapping("/online")
	public List<String> online() {
		return sessionManager.onlineList();
	}

}
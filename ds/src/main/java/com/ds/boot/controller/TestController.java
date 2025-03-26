package com.ds.boot.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ds
 * @date 2025/1/24
 * @description
 */
@RestController
@RequestMapping("test")
public class TestController {

	@RequestMapping("hello")
	public Map<String, Object> hello() {
		Map<String, Object> result = new HashMap<>();
		result.put("msg", "ok");
		result.put("code", 200);
		return result;
	}

}

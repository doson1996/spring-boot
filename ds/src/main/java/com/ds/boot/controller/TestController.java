package com.ds.boot.controller;

import com.ds.boot.annotion.Injection;
import com.ds.boot.service.TestService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

/**
 * @author ds
 * @date 2025/1/24
 * @description
 */
@RestController
@RequestMapping("test")
public class TestController {

	@Injection
//	@Resource
	private TestService testService;

	@RequestMapping("hello")
	public Map<String, Object> hello() {
		System.out.println("testService = " + testService);
		Map<String, Object> result = new HashMap<>();
		result.put("msg", "ok");
		result.put("code", 200);
		return result;
	}

}

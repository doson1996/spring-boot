package com.ds.boot.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.ds.boot.service.AsyncService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ds
 * @date 2025/1/24
 * @description
 */
@RestController
@RequestMapping("test")
public class TestController {

	@Resource
	private AsyncService asyncService;

	@RequestMapping("async")
	public Map<String, Object> async() {
		Map<String, Object> result = new HashMap<>();
		try {
			result = asyncService.async().get();
		} catch (Exception e) {
			System.err.println("e = " + e);
		}
		return result;
	}

}

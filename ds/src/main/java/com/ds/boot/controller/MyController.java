package com.ds.boot.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.ds.boot.service.MyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ds
 * @date 2025/5/29
 * @description
 */
@RestController
@RequestMapping("my")
public class MyController {

	@Resource
	private MyService myService;

	@RequestMapping("save")
	public Map<String, Object> save(String value) {
		boolean res = myService.insert(value);
		Map<String, Object> result = new HashMap<>();
		result.put("msg", res);
		result.put("code", 200);
		return result;
	}

}

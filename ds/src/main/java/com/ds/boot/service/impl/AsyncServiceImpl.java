package com.ds.boot.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.ds.boot.service.AsyncService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

/**
 * @author ds
 * @date 2025/5/22
 * @description
 */
@Service
public class AsyncServiceImpl implements AsyncService {

	@Async
	@Override
	public Future<Map<String, Object>> async() {
		System.out.println("hello...");
		Map<String, Object> result = new HashMap<>();
		result.put("msg", "ok");
		result.put("code", 200);
		return new AsyncResult<>(result);
	}

}

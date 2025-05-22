package com.ds.boot.service;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author ds
 * @date 2025/5/22
 * @description
 */
public interface AsyncService {

	/**
	 * 有返回值异步方法处理
	 *
	 * @return
	 */
	Future<Map<String, Object>> async();

}

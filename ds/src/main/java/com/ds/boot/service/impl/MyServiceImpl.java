package com.ds.boot.service.impl;

import javax.annotation.Resource;

import com.ds.boot.service.MyService;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author ds
 * @date 2025/5/29
 * @description
 */
@Service
public class MyServiceImpl implements MyService {

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Resource
	private SqlSessionTemplate sqlSessionTemplate;

	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean insert(String value) {
//		String sql = "insert into user values (" + value + ")";
		String sql = "insert into user(name) values (?)";
//		int update = jdbcTemplate.update(sql, value);
		int insert = sqlSessionTemplate.insert(sql, value);
		if ("ls".equals(value))
			throw new RuntimeException("ls ex");
		return insert > 0;
	}

}

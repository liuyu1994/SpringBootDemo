package com.springboot.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.springboot.dao.SysLogDao;
import com.springboot.domain.SysLog;

@Repository
public class SysLogDaoImp implements SysLogDao {

	@Autowired
	private SysLogDao sysLogDao;

	@Override
	public void saveSysLog(SysLog syslog) {
		sysLogDao.saveSysLog(syslog);
	}
}

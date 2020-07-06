package com.springboot.dao;

import com.springboot.domain.SysLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface SysLogDao {
	@Insert("insert into sys_log(id,username,operation,time,method,params,ip,createtime) values(#{id},#{username},#{operation},#{time},#{method},#{params},#{ip},#{createtime})")
	void saveSysLog(SysLog syslog);
}

package com.example.springboottest.modules.log.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.log.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志数据访问接口
 */
@Mapper
public interface LoginLogRepository extends BaseMapper<LoginLog> {
}

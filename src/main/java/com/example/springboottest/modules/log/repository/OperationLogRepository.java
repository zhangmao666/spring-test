package com.example.springboottest.modules.log.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.log.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志数据访问接口
 */
@Mapper
public interface OperationLogRepository extends BaseMapper<OperationLog> {
}

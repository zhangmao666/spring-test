package com.example.springboottest.modules.ai.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.ai.entity.AiModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: zm
 * @Created: 2025/12/2 上午10:49
 * @Description: ai模型数据接口
 */
@Mapper
public interface AiModelRepository extends BaseMapper<AiModel> {




}

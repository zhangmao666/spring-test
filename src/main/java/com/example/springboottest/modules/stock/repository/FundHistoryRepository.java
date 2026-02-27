package com.example.springboottest.modules.stock.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.stock.entity.FundHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FundHistoryRepository extends BaseMapper<FundHistory> {
}

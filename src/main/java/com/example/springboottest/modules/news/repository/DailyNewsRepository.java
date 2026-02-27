package com.example.springboottest.modules.news.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.news.entity.DailyNews;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyNewsRepository extends BaseMapper<DailyNews> {
}

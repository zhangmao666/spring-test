package com.example.springboottest.modules.news.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.news.entity.NewsSubscriber;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NewsSubscriberRepository extends BaseMapper<NewsSubscriber> {
}

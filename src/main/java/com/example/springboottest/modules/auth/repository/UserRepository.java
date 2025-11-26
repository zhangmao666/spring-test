package com.example.springboottest.modules.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(String username);
    
    @Select("SELECT * FROM users WHERE email = #{email}")
    User selectByEmail(String email);
    
    @Select("SELECT COUNT(1) FROM users WHERE username = #{username}")
    int existsByUsername(String username);
    
    @Select("SELECT COUNT(1) FROM users WHERE email = #{email}")
    int existsByEmail(String email);
    
    @Select("SELECT * FROM users WHERE username = #{username} AND status = #{status}")
    User selectByUsernameAndStatus(String username, Integer status);
    
    default Optional<User> findByUsername(String username) {
        return Optional.ofNullable(selectByUsername(username));
    }
    
    default Optional<User> findByEmail(String email) {
        return Optional.ofNullable(selectByEmail(email));
    }
    
    default Optional<User> findByUsernameAndStatus(String username, Integer status) {
        return Optional.ofNullable(selectByUsernameAndStatus(username, status));
    }
}

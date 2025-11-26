package com.example.springboottest.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    User selectByEmail(String email);
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) FROM users WHERE username = #{username}")
    int existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) FROM users WHERE email = #{email}")
    int existsByEmail(String email);
    
    /**
     * 根据用户名和状态查找用户
     * 
     * @param username 用户名
     * @param status 状态
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND status = #{status}")
    User selectByUsernameAndStatus(String username, Integer status);
    
    /**
     * 根据用户名查找用户（返回Optional）
     */
    default Optional<User> findByUsername(String username) {
        return Optional.ofNullable(selectByUsername(username));
    }
    
    /**
     * 根据邮箱查找用户（返回Optional）
     */
    default Optional<User> findByEmail(String email) {
        return Optional.ofNullable(selectByEmail(email));
    }
    
    /**
     * 根据用户名和状态查找用户（返回Optional）
     */
    default Optional<User> findByUsernameAndStatus(String username, Integer status) {
        return Optional.ofNullable(selectByUsernameAndStatus(username, status));
    }
}

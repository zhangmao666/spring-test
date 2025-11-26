package com.example.springboottest.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.CourseUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 课程系统用户Repository
 */
@Mapper
public interface CourseUserRepository extends BaseMapper<CourseUser> {
    
    /**
     * 根据用户名查找
     */
    @Select("SELECT * FROM course_users WHERE username = #{username}")
    CourseUser selectByUsername(@Param("username") String username);
    
    /**
     * 根据用户名查找（返回Optional）
     */
    default Optional<CourseUser> findByUsername(String username) {
        return Optional.ofNullable(selectByUsername(username));
    }
    
    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM course_users WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * 根据角色查找用户列表
     */
    @Select("SELECT * FROM course_users WHERE role = #{role} AND status = 1")
    List<CourseUser> selectByRole(@Param("role") String role);
    
    /**
     * 检查是否为教师
     */
    @Select("SELECT COUNT(1) > 0 FROM course_users WHERE id = #{id} AND role = 'TEACHER' AND status = 1")
    boolean isTeacher(@Param("id") Long id);
    
    /**
     * 检查是否为学生
     */
    @Select("SELECT COUNT(1) > 0 FROM course_users WHERE id = #{id} AND role = 'STUDENT' AND status = 1")
    boolean isStudent(@Param("id") Long id);
}

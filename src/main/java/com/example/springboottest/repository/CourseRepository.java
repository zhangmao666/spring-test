package com.example.springboottest.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程Repository
 */
@Mapper
public interface CourseRepository extends BaseMapper<Course> {
    
    /**
     * 根据教师ID查找课程
     */
    @Select("SELECT * FROM courses WHERE teacher_id = #{teacherId}")
    List<Course> selectByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 查找已发布的课程
     */
    @Select("SELECT * FROM courses WHERE status = 1")
    List<Course> selectPublishedCourses();
    
    /**
     * 分页查询已发布的课程
     */
    @Select("SELECT * FROM courses WHERE status = 1")
    IPage<Course> selectPublishedCoursesPage(Page<Course> page);
    
    /**
     * 根据名称模糊查询
     */
    @Select("SELECT * FROM courses WHERE name LIKE CONCAT('%', #{name}, '%') AND status = 1")
    List<Course> searchByName(@Param("name") String name);
    
    /**
     * 检查课程名称是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM courses WHERE name = #{name}")
    boolean existsByName(@Param("name") String name);
}

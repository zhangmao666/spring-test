package com.example.springboottest.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.CourseEnroll;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 选课记录Repository
 */
@Mapper
public interface CourseEnrollRepository extends BaseMapper<CourseEnroll> {
    
    /**
     * 检查学生是否已选某课程
     */
    @Select("SELECT COUNT(1) > 0 FROM course_enrolls WHERE student_id = #{studentId} AND course_id = #{courseId}")
    boolean existsByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    /**
     * 查询学生的选课记录
     */
    @Select("SELECT * FROM course_enrolls WHERE student_id = #{studentId}")
    List<CourseEnroll> selectByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 查询课程的选课记录
     */
    @Select("SELECT * FROM course_enrolls WHERE course_id = #{courseId}")
    List<CourseEnroll> selectByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 统计课程的选课人数
     */
    @Select("SELECT COUNT(1) FROM course_enrolls WHERE course_id = #{courseId}")
    int countByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 统计学生选课数量
     */
    @Select("SELECT COUNT(1) FROM course_enrolls WHERE student_id = #{studentId}")
    int countByStudentId(@Param("studentId") Long studentId);
}

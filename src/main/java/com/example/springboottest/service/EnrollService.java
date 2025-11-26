package com.example.springboottest.service;

import com.example.springboottest.DTO.course.EnrollDTO;
import com.example.springboottest.DTO.course.EnrollRequest;
import com.example.springboottest.entity.Course;
import com.example.springboottest.entity.CourseEnroll;
import com.example.springboottest.entity.CourseUser;
import com.example.springboottest.enums.CourseStatus;
import com.example.springboottest.repository.CourseEnrollRepository;
import com.example.springboottest.repository.CourseRepository;
import com.example.springboottest.repository.CourseUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 选课服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollService {
    
    private final CourseEnrollRepository courseEnrollRepository;
    private final CourseRepository courseRepository;
    private final CourseUserRepository courseUserRepository;
    
    /**
     * 选课
     */
    @Transactional
    public EnrollDTO enroll(EnrollRequest request) {
        Long studentId = request.getStudentId();
        Long courseId = request.getCourseId();
        
        // 校验学生是否存在
        if (!courseUserRepository.isStudent(studentId)) {
            throw new IllegalArgumentException("学生不存在或不是有效的学生: " + studentId);
        }
        
        // 校验课程是否存在
        Course course = courseRepository.selectById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在: " + courseId);
        }
        
        // 校验课程是否已发布
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException("课程未发布，无法选课");
        }
        
        // 检查是否已选课（防止重复选课）
        if (courseEnrollRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new IllegalArgumentException("您已选过该课程，不能重复选课");
        }
        
        // 创建选课记录
        CourseEnroll enroll = CourseEnroll.builder()
                .studentId(studentId)
                .courseId(courseId)
                .enrollTime(LocalDateTime.now())
                .build();
        
        courseEnrollRepository.insert(enroll);
        log.info("选课成功: studentId={}, courseId={}", studentId, courseId);
        
        return toDTO(enroll);
    }
    
    /**
     * 取消选课
     */
    @Transactional
    public void cancelEnroll(Long studentId, Long courseId) {
        if (!courseEnrollRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new IllegalArgumentException("未找到选课记录");
        }
        
        // 使用 MyBatis-Plus 的条件删除
        courseEnrollRepository.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CourseEnroll>()
                        .eq("student_id", studentId)
                        .eq("course_id", courseId)
        );
        
        log.info("取消选课成功: studentId={}, courseId={}", studentId, courseId);
    }
    
    /**
     * 获取学生的选课列表
     */
    public List<EnrollDTO> getStudentEnrollments(Long studentId) {
        return courseEnrollRepository.selectByStudentId(studentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取课程的选课学生列表
     */
    public List<EnrollDTO> getCourseEnrollments(Long courseId) {
        return courseEnrollRepository.selectByCourseId(courseId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查学生是否已选某课程
     */
    public boolean isEnrolled(Long studentId, Long courseId) {
        return courseEnrollRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }
    
    /**
     * 统计课程选课人数
     */
    public int countCourseEnrollments(Long courseId) {
        return courseEnrollRepository.countByCourseId(courseId);
    }
    
    /**
     * 统计学生选课数量
     */
    public int countStudentEnrollments(Long studentId) {
        return courseEnrollRepository.countByStudentId(studentId);
    }
    
    /**
     * 转换为DTO
     */
    private EnrollDTO toDTO(CourseEnroll enroll) {
        CourseUser student = courseUserRepository.selectById(enroll.getStudentId());
        Course course = courseRepository.selectById(enroll.getCourseId());
        
        return EnrollDTO.builder()
                .id(enroll.getId())
                .studentId(enroll.getStudentId())
                .studentName(student != null ? student.getUsername() : null)
                .courseId(enroll.getCourseId())
                .courseName(course != null ? course.getName() : null)
                .enrollTime(enroll.getEnrollTime())
                .build();
    }
}

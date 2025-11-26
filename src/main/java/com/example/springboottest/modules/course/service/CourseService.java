package com.example.springboottest.modules.course.service;

import com.example.springboottest.modules.course.dto.CourseDTO;
import com.example.springboottest.modules.course.dto.CreateCourseRequest;
import com.example.springboottest.modules.course.dto.UpdateCourseRequest;
import com.example.springboottest.modules.course.entity.Course;
import com.example.springboottest.modules.course.entity.CourseUser;
import com.example.springboottest.modules.course.enums.CourseStatus;
import com.example.springboottest.modules.course.repository.CourseEnrollRepository;
import com.example.springboottest.modules.course.repository.CourseRepository;
import com.example.springboottest.modules.course.repository.CourseUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final CourseUserRepository courseUserRepository;
    private final CourseEnrollRepository courseEnrollRepository;
    
    /**
     * 创建课程（草稿状态）
     */
    @Transactional
    public CourseDTO createCourse(CreateCourseRequest request) {
        // 校验教师是否存在
        if (!courseUserRepository.isTeacher(request.getTeacherId())) {
            throw new IllegalArgumentException("教师不存在或不是有效的教师: " + request.getTeacherId());
        }
        
        Course course = Course.builder()
                .name(request.getName())
                .teacherId(request.getTeacherId())
                .price(request.getPrice())
                .status(CourseStatus.DRAFT)
                .description(request.getDescription())
                .build();
        
        courseRepository.insert(course);
        log.info("创建课程成功: id={}, name={}", course.getId(), course.getName());
        
        return toDTO(course);
    }
    
    /**
     * 更新课程
     */
    @Transactional
    public CourseDTO updateCourse(Long courseId, UpdateCourseRequest request) {
        Course course = courseRepository.selectById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在: " + courseId);
        }
        
        // 如果修改教师，校验新教师是否存在
        if (request.getTeacherId() != null && !request.getTeacherId().equals(course.getTeacherId())) {
            if (!courseUserRepository.isTeacher(request.getTeacherId())) {
                throw new IllegalArgumentException("教师不存在或不是有效的教师: " + request.getTeacherId());
            }
            course.setTeacherId(request.getTeacherId());
        }
        
        if (request.getName() != null) {
            course.setName(request.getName());
        }
        if (request.getPrice() != null) {
            course.setPrice(request.getPrice());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        
        courseRepository.updateById(course);
        log.info("更新课程成功: id={}", courseId);
        
        return toDTO(course);
    }
    
    /**
     * 发布课程
     */
    @Transactional
    public CourseDTO publishCourse(Long courseId) {
        Course course = courseRepository.selectById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在: " + courseId);
        }
        
        // 校验教师是否存在
        if (!courseUserRepository.isTeacher(course.getTeacherId())) {
            throw new IllegalArgumentException("无法发布课程：教师不存在或已被禁用");
        }
        
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException("课程已发布");
        }
        
        course.setStatus(CourseStatus.PUBLISHED);
        courseRepository.updateById(course);
        log.info("发布课程成功: id={}, name={}", courseId, course.getName());
        
        return toDTO(course);
    }
    
    /**
     * 下架课程（变为草稿状态）
     */
    @Transactional
    public CourseDTO unpublishCourse(Long courseId) {
        Course course = courseRepository.selectById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在: " + courseId);
        }
        
        course.setStatus(CourseStatus.DRAFT);
        courseRepository.updateById(course);
        log.info("下架课程成功: id={}", courseId);
        
        return toDTO(course);
    }
    
    /**
     * 根据ID获取课程
     */
    public CourseDTO getCourseById(Long courseId) {
        Course course = courseRepository.selectById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在: " + courseId);
        }
        return toDTO(course);
    }
    
    /**
     * 获取所有课程
     */
    public List<CourseDTO> listAllCourses() {
        return courseRepository.selectList(null).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取已发布的课程
     */
    public List<CourseDTO> listPublishedCourses() {
        return courseRepository.selectPublishedCourses().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据教师ID获取课程
     */
    public List<CourseDTO> listCoursesByTeacher(Long teacherId) {
        return courseRepository.selectByTeacherId(teacherId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 搜索课程
     */
    public List<CourseDTO> searchCourses(String keyword) {
        return courseRepository.searchByName(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 删除课程
     */
    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.selectById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在: " + courseId);
        }
        
        // 检查是否有学生选课
        int enrollCount = courseEnrollRepository.countByCourseId(courseId);
        if (enrollCount > 0) {
            throw new IllegalArgumentException("课程已有学生选课，无法删除");
        }
        
        courseRepository.deleteById(courseId);
        log.info("删除课程成功: id={}", courseId);
    }
    
    /**
     * 转换为DTO
     */
    private CourseDTO toDTO(Course course) {
        CourseUser teacher = courseUserRepository.selectById(course.getTeacherId());
        int enrollCount = courseEnrollRepository.countByCourseId(course.getId());
        
        return CourseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .teacherId(course.getTeacherId())
                .teacherName(teacher != null ? teacher.getUsername() : null)
                .price(course.getPrice())
                .status(course.getStatus())
                .description(course.getDescription())
                .enrollCount(enrollCount)
                .createdAt(course.getCreatedAt())
                .build();
    }
}

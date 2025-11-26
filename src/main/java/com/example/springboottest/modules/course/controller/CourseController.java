package com.example.springboottest.modules.course.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.course.dto.CourseDTO;
import com.example.springboottest.modules.course.dto.CreateCourseRequest;
import com.example.springboottest.modules.course.dto.UpdateCourseRequest;
import com.example.springboottest.modules.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程控制器
 */
@Slf4j
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    
    /**
     * 创建课程（默认为草稿状态）
     */
    @PostMapping
    public ApiResponse<CourseDTO> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        log.info("创建课程: name={}, teacherId={}", request.getName(), request.getTeacherId());
        CourseDTO course = courseService.createCourse(request);
        return ApiResponse.success("课程创建成功", course);
    }
    
    /**
     * 更新课程
     */
    @PutMapping("/{courseId}")
    public ApiResponse<CourseDTO> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody UpdateCourseRequest request) {
        log.info("更新课程: courseId={}", courseId);
        CourseDTO course = courseService.updateCourse(courseId, request);
        return ApiResponse.success("课程更新成功", course);
    }
    
    /**
     * 发布课程
     */
    @PostMapping("/{courseId}/publish")
    public ApiResponse<CourseDTO> publishCourse(@PathVariable Long courseId) {
        log.info("发布课程: courseId={}", courseId);
        CourseDTO course = courseService.publishCourse(courseId);
        return ApiResponse.success("课程发布成功", course);
    }
    
    /**
     * 下架课程
     */
    @PostMapping("/{courseId}/unpublish")
    public ApiResponse<CourseDTO> unpublishCourse(@PathVariable Long courseId) {
        log.info("下架课程: courseId={}", courseId);
        CourseDTO course = courseService.unpublishCourse(courseId);
        return ApiResponse.success("课程已下架", course);
    }
    
    /**
     * 获取课程详情
     */
    @GetMapping("/{courseId}")
    public ApiResponse<CourseDTO> getCourse(@PathVariable Long courseId) {
        CourseDTO course = courseService.getCourseById(courseId);
        return ApiResponse.success(course);
    }
    
    /**
     * 获取所有课程（管理员视角）
     */
    @GetMapping("/all")
    public ApiResponse<List<CourseDTO>> listAllCourses() {
        return ApiResponse.success(courseService.listAllCourses());
    }
    
    /**
     * 获取已发布的课程（学生视角）
     */
    @GetMapping
    public ApiResponse<List<CourseDTO>> listPublishedCourses() {
        return ApiResponse.success(courseService.listPublishedCourses());
    }
    
    /**
     * 获取教师的课程
     */
    @GetMapping("/teacher/{teacherId}")
    public ApiResponse<List<CourseDTO>> listCoursesByTeacher(@PathVariable Long teacherId) {
        return ApiResponse.success(courseService.listCoursesByTeacher(teacherId));
    }
    
    /**
     * 搜索课程
     */
    @GetMapping("/search")
    public ApiResponse<List<CourseDTO>> searchCourses(@RequestParam String keyword) {
        return ApiResponse.success(courseService.searchCourses(keyword));
    }
    
    /**
     * 删除课程
     */
    @DeleteMapping("/{courseId}")
    public ApiResponse<String> deleteCourse(@PathVariable Long courseId) {
        log.info("删除课程: courseId={}", courseId);
        courseService.deleteCourse(courseId);
        return ApiResponse.success("课程已删除");
    }
}

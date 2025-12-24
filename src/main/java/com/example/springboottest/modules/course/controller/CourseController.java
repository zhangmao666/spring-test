package com.example.springboottest.modules.course.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.course.dto.CourseDTO;
import com.example.springboottest.modules.course.dto.CreateCourseRequest;
import com.example.springboottest.modules.course.dto.UpdateCourseRequest;
import com.example.springboottest.modules.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "课程管理", description = "课程的创建、更新、发布、查询等管理接口")
public class CourseController {

    private final CourseService courseService;

    /**
     * 创建课程（默认为草稿状态）
     */
    @Operation(summary = "创建课程", description = "创建新课程，默认为草稿状态")
    @PostMapping
    public ApiResponse<CourseDTO> createCourse(
            @Parameter(description = "课程创建请求", required = true) @Valid @RequestBody CreateCourseRequest request) {
        log.info("创建课程: name={}, teacherId={}", request.getName(), request.getTeacherId());
        CourseDTO course = courseService.createCourse(request);
        return ApiResponse.success("课程创建成功", course);
    }

    /**
     * 更新课程
     */
    @Operation(summary = "更新课程", description = "更新课程信息")
    @PutMapping("/{courseId}")
    public ApiResponse<CourseDTO> updateCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable Long courseId,
            @Parameter(description = "课程更新请求", required = true) @Valid @RequestBody UpdateCourseRequest request) {
        log.info("更新课程: courseId={}", courseId);
        CourseDTO course = courseService.updateCourse(courseId, request);
        return ApiResponse.success("课程更新成功", course);
    }

    /**
     * 发布课程
     */
    @Operation(summary = "发布课程", description = "将草稿状态的课程发布为正式课程")
    @PostMapping("/{courseId}/publish")
    public ApiResponse<CourseDTO> publishCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable Long courseId) {
        log.info("发布课程: courseId={}", courseId);
        CourseDTO course = courseService.publishCourse(courseId);
        return ApiResponse.success("课程发布成功", course);
    }

    /**
     * 下架课程
     */
    @Operation(summary = "下架课程", description = "将已发布的课程下架")
    @PostMapping("/{courseId}/unpublish")
    public ApiResponse<CourseDTO> unpublishCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable Long courseId) {
        log.info("下架课程: courseId={}", courseId);
        CourseDTO course = courseService.unpublishCourse(courseId);
        return ApiResponse.success("课程已下架", course);
    }

    /**
     * 获取课程详情
     */
    @Operation(summary = "获取课程详情", description = "根据课程ID获取课程详细信息")
    @GetMapping("/{courseId}")
    public ApiResponse<CourseDTO> getCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable Long courseId) {
        CourseDTO course = courseService.getCourseById(courseId);
        return ApiResponse.success(course);
    }

    /**
     * 获取所有课程（管理员视角）
     */
    @Operation(summary = "查询所有课程", description = "查询所有课程，包括草稿和已发布的课程（管理员使用）")
    @GetMapping("/all")
    public ApiResponse<List<CourseDTO>> listAllCourses() {
        return ApiResponse.success(courseService.listAllCourses());
    }

    /**
     * 获取已发布的课程（学生视角）
     */
    @Operation(summary = "查询已发布课程", description = "查询所有已发布的课程（学生使用）")
    @GetMapping
    public ApiResponse<List<CourseDTO>> listPublishedCourses() {
        return ApiResponse.success(courseService.listPublishedCourses());
    }

    /**
     * 获取教师的课程
     */
    @Operation(summary = "查询教师课程", description = "根据教师ID查询该教师的所有课程")
    @GetMapping("/teacher/{teacherId}")
    public ApiResponse<List<CourseDTO>> listCoursesByTeacher(
            @Parameter(description = "教师ID", required = true) @PathVariable Long teacherId) {
        return ApiResponse.success(courseService.listCoursesByTeacher(teacherId));
    }

    /**
     * 搜索课程
     */
    @Operation(summary = "搜索课程", description = "根据关键词搜索课程名称或描述")
    @GetMapping("/search")
    public ApiResponse<List<CourseDTO>> searchCourses(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword) {
        return ApiResponse.success(courseService.searchCourses(keyword));
    }

    /**
     * 删除课程
     */
    @Operation(summary = "删除课程", description = "根据课程ID删除课程")
    @DeleteMapping("/{courseId}")
    public ApiResponse<String> deleteCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable Long courseId) {
        log.info("删除课程: courseId={}", courseId);
        courseService.deleteCourse(courseId);
        return ApiResponse.success("课程已删除");
    }
}

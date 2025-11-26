package com.example.springboottest.controller;

import com.example.springboottest.DTO.ApiResponse;
import com.example.springboottest.DTO.course.EnrollDTO;
import com.example.springboottest.DTO.course.EnrollRequest;
import com.example.springboottest.service.EnrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选课控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/enrolls")
@RequiredArgsConstructor
public class EnrollController {
    
    private final EnrollService enrollService;
    
    /**
     * 选课
     */
    @PostMapping
    public ApiResponse<EnrollDTO> enroll(@Valid @RequestBody EnrollRequest request) {
        log.info("选课: studentId={}, courseId={}", request.getStudentId(), request.getCourseId());
        EnrollDTO enroll = enrollService.enroll(request);
        return ApiResponse.success("选课成功", enroll);
    }
    
    /**
     * 取消选课
     */
    @DeleteMapping
    public ApiResponse<String> cancelEnroll(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        log.info("取消选课: studentId={}, courseId={}", studentId, courseId);
        enrollService.cancelEnroll(studentId, courseId);
        return ApiResponse.success("取消选课成功");
    }
    
    /**
     * 获取学生的选课列表
     */
    @GetMapping("/student/{studentId}")
    public ApiResponse<List<EnrollDTO>> getStudentEnrollments(@PathVariable Long studentId) {
        return ApiResponse.success(enrollService.getStudentEnrollments(studentId));
    }
    
    /**
     * 获取课程的选课学生列表
     */
    @GetMapping("/course/{courseId}")
    public ApiResponse<List<EnrollDTO>> getCourseEnrollments(@PathVariable Long courseId) {
        return ApiResponse.success(enrollService.getCourseEnrollments(courseId));
    }
    
    /**
     * 检查是否已选课
     */
    @GetMapping("/check")
    public ApiResponse<Map<String, Object>> checkEnrollment(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        boolean enrolled = enrollService.isEnrolled(studentId, courseId);
        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("courseId", courseId);
        result.put("enrolled", enrolled);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取选课统计
     */
    @GetMapping("/stats/course/{courseId}")
    public ApiResponse<Map<String, Object>> getCourseStats(@PathVariable Long courseId) {
        int count = enrollService.countCourseEnrollments(courseId);
        Map<String, Object> result = new HashMap<>();
        result.put("courseId", courseId);
        result.put("enrollCount", count);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取学生选课统计
     */
    @GetMapping("/stats/student/{studentId}")
    public ApiResponse<Map<String, Object>> getStudentStats(@PathVariable Long studentId) {
        int count = enrollService.countStudentEnrollments(studentId);
        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("courseCount", count);
        return ApiResponse.success(result);
    }
}

package com.example.springboottest.service;

import com.example.springboottest.DTO.course.CourseUserDTO;
import com.example.springboottest.DTO.course.CreateCourseUserRequest;
import com.example.springboottest.entity.CourseUser;
import com.example.springboottest.enums.CourseUserRole;
import com.example.springboottest.repository.CourseUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程系统用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseUserService {
    
    private final CourseUserRepository courseUserRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 创建用户
     */
    @Transactional
    public CourseUserDTO createUser(CreateCourseUserRequest request) {
        // 检查用户名是否已存在
        if (courseUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
        }
        
        CourseUser user = CourseUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(1)
                .build();
        
        courseUserRepository.insert(user);
        log.info("创建课程系统用户成功: id={}, username={}, role={}", 
                user.getId(), user.getUsername(), user.getRole());
        
        return toDTO(user);
    }
    
    /**
     * 根据ID获取用户
     */
    public CourseUser getUserById(Long id) {
        return courseUserRepository.selectById(id);
    }
    
    /**
     * 根据用户名获取用户
     */
    public CourseUser getUserByUsername(String username) {
        return courseUserRepository.selectByUsername(username);
    }
    
    /**
     * 获取所有用户
     */
    public List<CourseUserDTO> listUsers() {
        return courseUserRepository.selectList(null).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据角色获取用户列表
     */
    public List<CourseUserDTO> listUsersByRole(CourseUserRole role) {
        return courseUserRepository.selectByRole(role.getCode()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有教师
     */
    public List<CourseUserDTO> listTeachers() {
        return listUsersByRole(CourseUserRole.TEACHER);
    }
    
    /**
     * 获取所有学生
     */
    public List<CourseUserDTO> listStudents() {
        return listUsersByRole(CourseUserRole.STUDENT);
    }
    
    /**
     * 检查是否为教师
     */
    public boolean isTeacher(Long userId) {
        return courseUserRepository.isTeacher(userId);
    }
    
    /**
     * 检查是否为学生
     */
    public boolean isStudent(Long userId) {
        return courseUserRepository.isStudent(userId);
    }
    
    /**
     * 启用用户
     */
    @Transactional
    public void enableUser(Long userId) {
        CourseUser user = courseUserRepository.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }
        user.setStatus(1);
        courseUserRepository.updateById(user);
        log.info("启用用户: id={}", userId);
    }
    
    /**
     * 禁用用户
     */
    @Transactional
    public void disableUser(Long userId) {
        CourseUser user = courseUserRepository.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }
        user.setStatus(0);
        courseUserRepository.updateById(user);
        log.info("禁用用户: id={}", userId);
    }
    
    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId) {
        courseUserRepository.deleteById(userId);
        log.info("删除用户: id={}", userId);
    }
    
    /**
     * 转换为DTO
     */
    private CourseUserDTO toDTO(CourseUser user) {
        return CourseUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

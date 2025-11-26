package com.example.springboottest.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联Repository
 */
@Mapper
public interface UserRoleRepository extends BaseMapper<UserRole> {

    /**
     * 查询用户的所有角色ID
     */
    @Select("SELECT role_id FROM user_roles WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(Long userId);

    /**
     * 查询拥有指定角色的所有用户ID
     */
    @Select("SELECT user_id FROM user_roles WHERE role_id = #{roleId}")
    List<Long> selectUserIdsByRoleId(Long roleId);

    /**
     * 根据角色编码查询用户ID列表
     */
    @Select("SELECT ur.user_id FROM user_roles ur " +
            "INNER JOIN roles r ON ur.role_id = r.id " +
            "WHERE r.role_code = #{roleCode} AND r.status = 1")
    List<Long> selectUserIdsByRoleCode(String roleCode);

    /**
     * 查询用户的所有角色编码
     */
    @Select("SELECT r.role_code FROM user_roles ur " +
            "INNER JOIN roles r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectRoleCodesByUserId(Long userId);
}

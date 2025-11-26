package com.example.springboottest.modules.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.auth.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联Repository
 */
@Mapper
public interface UserRoleRepository extends BaseMapper<UserRole> {

    @Select("SELECT role_id FROM user_roles WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(Long userId);

    @Select("SELECT user_id FROM user_roles WHERE role_id = #{roleId}")
    List<Long> selectUserIdsByRoleId(Long roleId);

    @Select("SELECT ur.user_id FROM user_roles ur " +
            "INNER JOIN roles r ON ur.role_id = r.id " +
            "WHERE r.role_code = #{roleCode} AND r.status = 1")
    List<Long> selectUserIdsByRoleCode(String roleCode);

    @Select("SELECT r.role_code FROM user_roles ur " +
            "INNER JOIN roles r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectRoleCodesByUserId(Long userId);
}

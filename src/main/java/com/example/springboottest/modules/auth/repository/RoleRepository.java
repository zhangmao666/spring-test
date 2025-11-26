package com.example.springboottest.modules.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.auth.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 角色Repository
 */
@Mapper
public interface RoleRepository extends BaseMapper<Role> {

    @Select("SELECT * FROM roles WHERE role_code = #{roleCode} AND status = 1")
    Role selectByRoleCode(String roleCode);

    default Optional<Role> findByRoleCode(String roleCode) {
        return Optional.ofNullable(selectByRoleCode(roleCode));
    }

    @Select("SELECT * FROM roles WHERE status = 1 ORDER BY created_at DESC")
    List<Role> selectAllActive();
}

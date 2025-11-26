package com.example.springboottest.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 角色Repository
 */
@Mapper
public interface RoleRepository extends BaseMapper<Role> {

    /**
     * 根据角色编码查询角色
     */
    @Select("SELECT * FROM roles WHERE role_code = #{roleCode} AND status = 1")
    Role selectByRoleCode(String roleCode);

    /**
     * 根据角色编码查询角色（Optional包装）
     */
    default Optional<Role> findByRoleCode(String roleCode) {
        return Optional.ofNullable(selectByRoleCode(roleCode));
    }

    /**
     * 查询所有启用的角色
     */
    @Select("SELECT * FROM roles WHERE status = 1 ORDER BY created_at DESC")
    List<Role> selectAllActive();
}

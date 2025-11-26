package com.example.springboottest.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.entity.Dict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DictRepository extends BaseMapper<Dict> {

    /**
     * 根据字典编码查找字典
     */
    @Select("SELECT * FROM dict WHERE dict_code = #{dictCode}")
    Dict selectByDictCode(String dictCode);

    /**
     * 检查字典编码是否存在
     */
    @Select("SELECT COUNT(1) FROM dict WHERE dict_code = #{dictCode}")
    boolean existsByDictCode(String dictCode);

    /**
     * 根据字典名称模糊查询
     */
    @Select("SELECT * FROM dict WHERE dict_name LIKE CONCAT('%', #{dictName}, '%')")
    List<Dict> findByDictNameContaining(String dictName);

    /**
     * 根据状态查找字典
     */
    @Select("SELECT * FROM dict WHERE status = #{status}")
    List<Dict> findByStatus(Integer status);

    /**
     * 分页查询字典，支持按名称和状态过滤
     */
    @Select("<script>" +
            "SELECT * FROM dict WHERE 1=1 " +
            "<if test='dictName != null and dictName != \"\"'>AND dict_name LIKE CONCAT('%', #{dictName}, '%')</if> " +
            "<if test='dictCode != null and dictCode != \"\"'>AND dict_code LIKE CONCAT('%', #{dictCode}, '%')</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<Dict> findByConditions(Page<Dict> page, String dictCode, String dictName, Integer status);

    /**
     * 统计指定状态的字典数量
     */
    @Select("SELECT COUNT(1) FROM dict WHERE status = #{status}")
    long countByStatus(Integer status);

    /**
     * 根据字典编码查找字典（返回Optional）
     */
    default Optional<Dict> findByDictCode(String dictCode) {
        return Optional.ofNullable(selectByDictCode(dictCode));
    }
}

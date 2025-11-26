package com.example.springboottest.modules.dict.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.modules.dict.entity.Dict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DictRepository extends BaseMapper<Dict> {

    @Select("SELECT * FROM dict WHERE dict_code = #{dictCode}")
    Dict selectByDictCode(String dictCode);

    @Select("SELECT COUNT(1) FROM dict WHERE dict_code = #{dictCode}")
    boolean existsByDictCode(String dictCode);

    @Select("SELECT * FROM dict WHERE dict_name LIKE CONCAT('%', #{dictName}, '%')")
    List<Dict> findByDictNameContaining(String dictName);

    @Select("SELECT * FROM dict WHERE status = #{status}")
    List<Dict> findByStatus(Integer status);

    @Select("<script>SELECT * FROM dict WHERE 1=1 " +
            "<if test='dictName != null and dictName != \"\"'>AND dict_name LIKE CONCAT('%', #{dictName}, '%')</if> " +
            "<if test='dictCode != null and dictCode != \"\"'>AND dict_code LIKE CONCAT('%', #{dictCode}, '%')</if> " +
            "<if test='status != null'>AND status = #{status}</if> ORDER BY create_time DESC</script>")
    IPage<Dict> findByConditions(Page<Dict> page, String dictCode, String dictName, Integer status);

    @Select("SELECT COUNT(1) FROM dict WHERE status = #{status}")
    long countByStatus(Integer status);

    default Optional<Dict> findByDictCode(String dictCode) {
        return Optional.ofNullable(selectByDictCode(dictCode));
    }
}

package com.example.springboottest.modules.prompt.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.modules.prompt.entity.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PromptTemplateRepository extends BaseMapper<PromptTemplate> {

    @Select("SELECT * FROM prompt_template WHERE prompt_code = #{promptCode} LIMIT 1")
    PromptTemplate selectByPromptCode(@Param("promptCode") String promptCode);

    @Select("SELECT * FROM prompt_template WHERE prompt_code = #{promptCode} AND status = 1 LIMIT 1")
    PromptTemplate selectActiveByPromptCode(@Param("promptCode") String promptCode);

    @Select("SELECT COUNT(1) > 0 FROM prompt_template WHERE prompt_code = #{promptCode}")
    boolean existsByPromptCode(@Param("promptCode") String promptCode);

    @Select("""
            <script>
            SELECT * FROM prompt_template
            WHERE 1 = 1
            <if test='promptCode != null and promptCode != ""'>
              AND prompt_code LIKE CONCAT('%', #{promptCode}, '%')
            </if>
            <if test='promptName != null and promptName != ""'>
              AND prompt_name LIKE CONCAT('%', #{promptName}, '%')
            </if>
            <if test='promptType != null and promptType != ""'>
              AND prompt_type = #{promptType}
            </if>
            <if test='status != null'>
              AND status = #{status}
            </if>
            ORDER BY update_time DESC, id DESC
            </script>
            """)
    IPage<PromptTemplate> findByConditions(Page<PromptTemplate> page,
                                           @Param("promptCode") String promptCode,
                                           @Param("promptName") String promptName,
                                           @Param("promptType") String promptType,
                                           @Param("status") Integer status);

    @Select("SELECT * FROM prompt_template WHERE status = 1 ORDER BY update_time DESC, id DESC")
    List<PromptTemplate> findAllActive();
}

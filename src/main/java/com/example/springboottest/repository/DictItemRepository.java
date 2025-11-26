package com.example.springboottest.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.entity.DictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DictItemRepository extends BaseMapper<DictItem> {

    /**
     * 根据字典ID查找所有字典项
     */
    @Select("SELECT * FROM dict_item WHERE dict_id = #{dictId} ORDER BY item_sort ASC, create_time ASC")
    List<DictItem> findByDictId(Long dictId);

    /**
     * 根据字典ID和状态查找字典项
     */
    @Select("SELECT * FROM dict_item WHERE dict_id = #{dictId} AND status = #{status} ORDER BY item_sort ASC, create_time ASC")
    List<DictItem> findByDictIdAndStatus(Long dictId, Integer status);

    /**
     * 根据字典项值查找
     */
    @Select("SELECT * FROM dict_item WHERE item_value = #{itemValue}")
    List<DictItem> findByItemValue(String itemValue);

    /**
     * 根据字典ID和字典项值查找唯一记录
     */
    @Select("SELECT * FROM dict_item WHERE dict_id = #{dictId} AND item_value = #{itemValue}")
    DictItem findByDictIdAndItemValue(Long dictId, String itemValue);

    /**
     * 检查字典项值在指定字典中是否存在
     */
    @Select("SELECT COUNT(1) FROM dict_item WHERE dict_id = #{dictId} AND item_value = #{itemValue}")
    boolean existsByDictIdAndItemValue(Long dictId, String itemValue);

    /**
     * 分页查询字典项
     */
    @Select("<script>" +
            "SELECT * FROM dict_item WHERE 1=1 " +
            "<if test='dictId != null'>AND dict_id = #{dictId}</if> " +
            "<if test='itemLabel != null and itemLabel != \"\"'>AND item_label LIKE CONCAT('%', #{itemLabel}, '%')</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "ORDER BY item_sort ASC, create_time ASC" +
            "</script>")
    IPage<DictItem> findByConditions(Page<DictItem> page, Long dictId, String itemLabel, Integer status);

    /**
     * 统计指定字典的字典项数量
     */
    @Select("SELECT COUNT(1) FROM dict_item WHERE dict_id = #{dictId}")
    long countByDictId(Long dictId);

    /**
     * 删除指定字典的所有字典项
     */
    @Select("DELETE FROM dict_item WHERE dict_id = #{dictId}")
    int deleteByDictId(Long dictId);

    /**
     * 获取指定字典的最大排序号
     */
    @Select("SELECT COALESCE(MAX(item_sort), 0) FROM dict_item WHERE dict_id = #{dictId}")
    Integer getMaxSortByDictId(Long dictId);
}

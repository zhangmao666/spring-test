package com.example.springboottest.modules.dict.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.modules.dict.entity.DictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DictItemRepository extends BaseMapper<DictItem> {

    @Select("SELECT * FROM dict_item WHERE dict_id = #{dictId} ORDER BY item_sort ASC, create_time ASC")
    List<DictItem> findByDictId(Long dictId);

    @Select("SELECT * FROM dict_item WHERE dict_id = #{dictId} AND status = #{status} ORDER BY item_sort ASC, create_time ASC")
    List<DictItem> findByDictIdAndStatus(Long dictId, Integer status);

    @Select("SELECT * FROM dict_item WHERE item_value = #{itemValue}")
    List<DictItem> findByItemValue(String itemValue);

    @Select("SELECT * FROM dict_item WHERE dict_id = #{dictId} AND item_value = #{itemValue}")
    DictItem findByDictIdAndItemValue(Long dictId, String itemValue);

    @Select("SELECT COUNT(1) FROM dict_item WHERE dict_id = #{dictId} AND item_value = #{itemValue}")
    boolean existsByDictIdAndItemValue(Long dictId, String itemValue);

    @Select("<script>SELECT * FROM dict_item WHERE 1=1 " +
            "<if test='dictId != null'>AND dict_id = #{dictId}</if> " +
            "<if test='itemLabel != null and itemLabel != \"\"'>AND item_label LIKE CONCAT('%', #{itemLabel}, '%')</if> " +
            "<if test='status != null'>AND status = #{status}</if> ORDER BY item_sort ASC, create_time ASC</script>")
    IPage<DictItem> findByConditions(Page<DictItem> page, Long dictId, String itemLabel, Integer status);

    @Select("SELECT COUNT(1) FROM dict_item WHERE dict_id = #{dictId}")
    long countByDictId(Long dictId);

    @Select("DELETE FROM dict_item WHERE dict_id = #{dictId}")
    int deleteByDictId(Long dictId);

    @Select("SELECT COALESCE(MAX(item_sort), 0) FROM dict_item WHERE dict_id = #{dictId}")
    Integer getMaxSortByDictId(Long dictId);
}

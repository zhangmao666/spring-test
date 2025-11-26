package com.example.springboottest.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.entity.DeviceType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DeviceTypeRepository extends BaseMapper<DeviceType> {
    
    /**
     * 根据名称查找设备类型
     */
    @Select("SELECT * FROM device_type WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<DeviceType> findByNameContaining(String name);
    
    /**
     * 根据状态查找设备类型
     */
    @Select("SELECT * FROM device_type WHERE status = #{status}")
    List<DeviceType> findByStatus(Integer status);
    
    /**
     * 根据全名查找设备类型
     */
    @Select("SELECT * FROM device_type WHERE full_name = #{fullName}")
    DeviceType selectByFullName(String fullName);
    
    /**
     * 根据类型代码查找设备类型
     */
    @Select("SELECT * FROM device_type WHERE tb_type_code = #{tbTypeCode}")
    List<DeviceType> findByTbTypeCode(String tbTypeCode);
    
    /**
     * 分页查询活跃的设备类型（状态为1）
     */
    @Select("SELECT * FROM device_type WHERE status = #{status} ORDER BY create_time DESC")
    IPage<DeviceType> findByStatusOrderByCreateTimeDesc(Page<DeviceType> page, Integer status);
    
    /**
     * 自定义查询：根据名称和状态查找
     */
    @Select("<script>" +
            "SELECT * FROM device_type WHERE 1=1 " +
            "<if test='name != null'>AND name LIKE CONCAT('%', #{name}, '%')</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<DeviceType> findByNameAndStatus(Page<DeviceType> page, String name, Integer status);
    
    /**
     * 检查设备类型代码是否存在
     */
    @Select("SELECT COUNT(1) FROM device_type WHERE device_type_code = #{deviceTypeCode}")
    boolean existsByDeviceTypeCode(String deviceTypeCode);
    
    /**
     * 统计指定状态的设备类型数量
     */
    @Select("SELECT COUNT(1) FROM device_type WHERE status = #{status}")
    long countByStatus(Integer status);
    
    /**
     * 根据全名查找设备类型（返回Optional）
     */
    default Optional<DeviceType> findByFullName(String fullName) {
        return Optional.ofNullable(selectByFullName(fullName));
    }
}

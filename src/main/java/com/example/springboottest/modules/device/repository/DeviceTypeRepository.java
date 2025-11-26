package com.example.springboottest.modules.device.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.modules.device.entity.DeviceType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DeviceTypeRepository extends BaseMapper<DeviceType> {
    
    @Select("SELECT * FROM device_type WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<DeviceType> findByNameContaining(String name);
    
    @Select("SELECT * FROM device_type WHERE status = #{status}")
    List<DeviceType> findByStatus(Integer status);
    
    @Select("SELECT * FROM device_type WHERE full_name = #{fullName}")
    DeviceType selectByFullName(String fullName);
    
    @Select("SELECT * FROM device_type WHERE tb_type_code = #{tbTypeCode}")
    List<DeviceType> findByTbTypeCode(String tbTypeCode);
    
    @Select("SELECT * FROM device_type WHERE status = #{status} ORDER BY create_time DESC")
    IPage<DeviceType> findByStatusOrderByCreateTimeDesc(Page<DeviceType> page, Integer status);
    
    @Select("<script>SELECT * FROM device_type WHERE 1=1 " +
            "<if test='name != null'>AND name LIKE CONCAT('%', #{name}, '%')</if> " +
            "<if test='status != null'>AND status = #{status}</if> ORDER BY create_time DESC</script>")
    IPage<DeviceType> findByNameAndStatus(Page<DeviceType> page, String name, Integer status);
    
    @Select("SELECT COUNT(1) FROM device_type WHERE device_type_code = #{deviceTypeCode}")
    boolean existsByDeviceTypeCode(String deviceTypeCode);
    
    @Select("SELECT COUNT(1) FROM device_type WHERE status = #{status}")
    long countByStatus(Integer status);
    
    default Optional<DeviceType> findByFullName(String fullName) {
        return Optional.ofNullable(selectByFullName(fullName));
    }
}

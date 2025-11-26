package com.example.springboottest.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.DTO.DeviceTypeQueryRequest;
import com.example.springboottest.DTO.DeviceTypeRequest;
import com.example.springboottest.DTO.DeviceTypeResponse;
import com.example.springboottest.entity.DeviceType;
import com.example.springboottest.repository.DeviceTypeRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeviceTypeService {
    
    @Autowired
    private DeviceTypeRepository deviceTypeRepository;
    
    /**
     * 创建设备类型
     */
    public int createDeviceType(DeviceTypeRequest request) {
        // 检查设备类型代码是否已存在
        if (deviceTypeRepository.existsByDeviceTypeCode(request.getDeviceTypeCode())) {
            throw new RuntimeException("设备类型代码已存在: " + request.getDeviceTypeCode());
        }
        
        DeviceType deviceType = new DeviceType();
        BeanUtils.copyProperties(request, deviceType);
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        deviceType.setCreateTime(now);
        deviceType.setUpdateTime(now);
        
        int insert = deviceTypeRepository.insert(deviceType);
        return insert;
    }
    
    /**
     * 根据ID获取设备类型
     */
    @Cacheable(value = "deviceTypeCache", key = "#deviceTypeCode")
    @Transactional(readOnly = true)
    public DeviceTypeResponse getDeviceTypeById(String deviceTypeCode) {
        System.out.println("没有走缓存。。。");
        DeviceType deviceType = deviceTypeRepository.selectById(deviceTypeCode);
        if (deviceType != null) {
            return new DeviceTypeResponse(deviceType);
        }
        throw new RuntimeException("设备类型不存在: " + deviceTypeCode);
    }
    
    /**
     * 更新设备类型
     */
    @CacheEvict(value = "deviceTypeCache", key = "#deviceTypeCode")
    public DeviceTypeResponse updateDeviceType(String deviceTypeCode, DeviceTypeRequest request) {
        DeviceType existingDeviceType = deviceTypeRepository.selectById(deviceTypeCode);
        if (existingDeviceType == null) {
            throw new RuntimeException("设备类型不存在: " + deviceTypeCode);
        }
        
        // 复制属性，但保留原有的创建时间和创建人
        LocalDateTime originalCreateTime = existingDeviceType.getCreateTime();
        Long originalCreateBy = existingDeviceType.getCreateBy();
        
        BeanUtils.copyProperties(request, existingDeviceType);
        existingDeviceType.setDeviceTypeCode(deviceTypeCode); // 确保ID不被覆盖
        existingDeviceType.setCreateTime(originalCreateTime);
        existingDeviceType.setCreateBy(originalCreateBy);
        existingDeviceType.setUpdateTime(LocalDateTime.now());
        
        deviceTypeRepository.updateById(existingDeviceType);
        return new DeviceTypeResponse(existingDeviceType);
    }
    
    /**
     * 删除设备类型
     */
    @CacheEvict(value = "deviceTypeCache", key = "#deviceTypeCode")
    public void deleteDeviceType(String deviceTypeCode) {
        if (!deviceTypeRepository.existsByDeviceTypeCode(deviceTypeCode)) {
            throw new RuntimeException("设备类型不存在: " + deviceTypeCode);
        }
        deviceTypeRepository.deleteById(deviceTypeCode);
    }
    
    /**
     * 分页查询设备类型
     */
    @Transactional(readOnly = true)
    public IPage<DeviceTypeResponse> queryDeviceTypes(DeviceTypeQueryRequest queryRequest) {
        // 创建MyBatis Plus分页对象
        Page<DeviceType> page = new Page<>(queryRequest.getPage() + 1, queryRequest.getSize());
        
        // 执行查询
        IPage<DeviceType> deviceTypePage = deviceTypeRepository.findByNameAndStatus(
            page,
            queryRequest.getName(), 
            queryRequest.getStatus()
        );
        
        // 转换为响应对象
        return deviceTypePage.convert(DeviceTypeResponse::new);
    }
    
    /**
     * 获取所有设备类型
     */
    @Transactional(readOnly = true)
    public List<DeviceTypeResponse> getAllDeviceTypes() {
        List<DeviceType> deviceTypes = deviceTypeRepository.selectList(null);
        return deviceTypes.stream()
                .map(DeviceTypeResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据状态获取设备类型
     */
    @Transactional(readOnly = true)
    public List<DeviceTypeResponse> getDeviceTypesByStatus(Integer status) {
        List<DeviceType> deviceTypes = deviceTypeRepository.findByStatus(status);
        return deviceTypes.stream()
                .map(DeviceTypeResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据名称搜索设备类型
     */
    @Transactional(readOnly = true)
    public List<DeviceTypeResponse> searchDeviceTypesByName(String name) {
        List<DeviceType> deviceTypes = deviceTypeRepository.findByNameContaining(name);
        return deviceTypes.stream()
                .map(DeviceTypeResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 统计指定状态的设备类型数量
     */
    @Transactional(readOnly = true)
    public long countByStatus(Integer status) {
        return deviceTypeRepository.countByStatus(status);
    }
    
    /**
     * 批量删除设备类型
     */
    @CacheEvict(value = "deviceTypeCache", allEntries = true)
    public void batchDeleteDeviceTypes(List<String> deviceTypeCodes) {
        // 先检查所有设备类型是否存在
        List<DeviceType> deviceTypes = deviceTypeRepository.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DeviceType>()
                .in("device_type_code", deviceTypeCodes)
        );
        if (deviceTypes.size() != deviceTypeCodes.size()) {
            throw new RuntimeException("部分设备类型不存在");
        }
        // deleteBatchIds需要传入ID列表（主键列表）
        deviceTypeRepository.deleteBatchIds(deviceTypeCodes);
    }

    public void getdData(){

    }

}

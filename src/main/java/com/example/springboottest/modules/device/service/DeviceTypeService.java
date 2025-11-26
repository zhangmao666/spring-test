package com.example.springboottest.modules.device.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.modules.device.dto.*;
import com.example.springboottest.modules.device.entity.DeviceType;
import com.example.springboottest.modules.device.repository.DeviceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceTypeService {

    private final DeviceTypeRepository deviceTypeRepository;

    public String createDeviceType(DeviceTypeRequest request) {
        if (deviceTypeRepository.existsByDeviceTypeCode(request.getDeviceTypeCode())) {
            throw new RuntimeException("设备类型代码已存在: " + request.getDeviceTypeCode());
        }
        DeviceType deviceType = new DeviceType();
        BeanUtils.copyProperties(request, deviceType);
        if (deviceType.getStatus() == null) deviceType.setStatus(1);
        deviceType.setCreateTime(LocalDateTime.now());
        deviceType.setUpdateTime(LocalDateTime.now());
        deviceTypeRepository.insert(deviceType);
        return deviceType.getDeviceTypeCode();
    }

    @Transactional(readOnly = true)
    public DeviceTypeResponse getDeviceTypeByCode(String deviceTypeCode) {
        DeviceType deviceType = deviceTypeRepository.selectById(deviceTypeCode);
        if (deviceType == null) throw new RuntimeException("设备类型不存在: " + deviceTypeCode);
        return new DeviceTypeResponse(deviceType);
    }

    public DeviceTypeResponse updateDeviceType(String deviceTypeCode, DeviceTypeRequest request) {
        DeviceType existingDeviceType = deviceTypeRepository.selectById(deviceTypeCode);
        if (existingDeviceType == null) throw new RuntimeException("设备类型不存在: " + deviceTypeCode);
        LocalDateTime originalCreateTime = existingDeviceType.getCreateTime();
        Long originalCreateBy = existingDeviceType.getCreateBy();
        BeanUtils.copyProperties(request, existingDeviceType);
        existingDeviceType.setDeviceTypeCode(deviceTypeCode);
        existingDeviceType.setCreateTime(originalCreateTime);
        existingDeviceType.setCreateBy(originalCreateBy);
        existingDeviceType.setUpdateTime(LocalDateTime.now());
        deviceTypeRepository.updateById(existingDeviceType);
        return new DeviceTypeResponse(existingDeviceType);
    }

    public void deleteDeviceType(String deviceTypeCode) {
        DeviceType deviceType = deviceTypeRepository.selectById(deviceTypeCode);
        if (deviceType == null) throw new RuntimeException("设备类型不存在: " + deviceTypeCode);
        deviceTypeRepository.deleteById(deviceTypeCode);
    }

    @Transactional(readOnly = true)
    public IPage<DeviceTypeResponse> queryDeviceTypes(DeviceTypeQueryRequest queryRequest) {
        Page<DeviceType> page = new Page<>(queryRequest.getPage() + 1, queryRequest.getSize());
        IPage<DeviceType> deviceTypePage = deviceTypeRepository.findByNameAndStatus(page, queryRequest.getName(), queryRequest.getStatus());
        return deviceTypePage.convert(DeviceTypeResponse::new);
    }

    @Transactional(readOnly = true)
    public List<DeviceTypeResponse> getAllActiveDeviceTypes() {
        return deviceTypeRepository.findByStatus(1).stream().map(DeviceTypeResponse::new).collect(Collectors.toList());
    }
}

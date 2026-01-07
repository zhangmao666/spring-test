package com.example.springboottest.modules.device.mapper;

import com.example.springboottest.modules.device.dto.DeviceTypeResponse;
import com.example.springboottest.modules.device.entity.DeviceType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * DeviceType 对象转换器
 * 使用 MapStruct 自动生成实现类
 */
@Mapper(componentModel = "spring")
public interface DeviceTypeMapper {

    DeviceTypeMapper INSTANCE = Mappers.getMapper(DeviceTypeMapper.class);

    /**
     * Entity 转 Response DTO
     */
    DeviceTypeResponse toResponse(DeviceType deviceType);

    /**
     * Entity 列表转 Response DTO 列表
     */
    List<DeviceTypeResponse> toResponseList(List<DeviceType> deviceTypes);
}

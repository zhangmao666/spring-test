package com.example.springboottest.convertor;

import com.example.springboottest.DTO.DeviceTypeDTO;
import com.example.springboottest.DTO.DeviceTypeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author: zm
 * @Created: 2025/9/29 下午3:56
 * @Description: 设备类型转换类
 */
@Mapper(componentModel = "default")  // 明确指定为MapStruct，不是Spring Bean
public interface DeviceTypeConvertor {

    DeviceTypeConvertor INSTANCE = Mappers.getMapper(DeviceTypeConvertor.class);

    DeviceTypeDTO toDTO(DeviceTypeResponse response);


}

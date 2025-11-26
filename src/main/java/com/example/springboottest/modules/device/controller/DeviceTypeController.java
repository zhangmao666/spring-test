package com.example.springboottest.modules.device.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.device.dto.*;
import com.example.springboottest.modules.device.service.DeviceTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device-types")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
public class DeviceTypeController {

    private final DeviceTypeService deviceTypeService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createDeviceType(@Valid @RequestBody DeviceTypeRequest request) {
        try {
            String deviceTypeCode = deviceTypeService.createDeviceType(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("创建设备类型成功", deviceTypeCode));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("创建设备类型失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{deviceTypeCode}")
    public ResponseEntity<ApiResponse<DeviceTypeResponse>> getDeviceTypeByCode(@PathVariable String deviceTypeCode) {
        try {
            DeviceTypeResponse response = deviceTypeService.getDeviceTypeByCode(deviceTypeCode);
            return ResponseEntity.ok(ApiResponse.success("获取设备类型成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("获取设备类型失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{deviceTypeCode}")
    public ResponseEntity<ApiResponse<DeviceTypeResponse>> updateDeviceType(@PathVariable String deviceTypeCode, @Valid @RequestBody DeviceTypeRequest request) {
        try {
            DeviceTypeResponse response = deviceTypeService.updateDeviceType(deviceTypeCode, request);
            return ResponseEntity.ok(ApiResponse.success("更新设备类型成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("更新设备类型失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{deviceTypeCode}")
    public ResponseEntity<ApiResponse<Void>> deleteDeviceType(@PathVariable String deviceTypeCode) {
        try {
            deviceTypeService.deleteDeviceType(deviceTypeCode);
            return ResponseEntity.ok(ApiResponse.success("删除设备类型成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("删除设备类型失败: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<IPage<DeviceTypeResponse>>> queryDeviceTypes(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            DeviceTypeQueryRequest queryRequest = new DeviceTypeQueryRequest();
            queryRequest.setName(name);
            queryRequest.setStatus(status);
            queryRequest.setPage(page);
            queryRequest.setSize(size);
            IPage<DeviceTypeResponse> response = deviceTypeService.queryDeviceTypes(queryRequest);
            return ResponseEntity.ok(ApiResponse.success("查询设备类型成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("查询设备类型失败: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DeviceTypeResponse>>> getAllActiveDeviceTypes() {
        try {
            List<DeviceTypeResponse> response = deviceTypeService.getAllActiveDeviceTypes();
            return ResponseEntity.ok(ApiResponse.success("获取所有启用设备类型成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取所有启用设备类型失败: " + e.getMessage()));
        }
    }
}

package com.example.springboottest.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.DTO.ApiResponse;
import com.example.springboottest.DTO.DeviceTypeQueryRequest;
import com.example.springboottest.DTO.DeviceTypeRequest;
import com.example.springboottest.DTO.DeviceTypeResponse;
import com.example.springboottest.service.DeviceTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device-types")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class DeviceTypeController {
    
    @Autowired
    private DeviceTypeService deviceTypeService;
    
    /**
     * 创建设备类型
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createDeviceType(@Valid @RequestBody DeviceTypeRequest request) {
        try {
            int insert = deviceTypeService.createDeviceType(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("创建设备类型成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建设备类型失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取设备类型
     */
    @GetMapping("/{deviceTypeCode}")
    public ResponseEntity<ApiResponse<DeviceTypeResponse>> getDeviceType(@PathVariable String deviceTypeCode) {
        try {
            DeviceTypeResponse response = deviceTypeService.getDeviceTypeById(deviceTypeCode);
            return ResponseEntity.ok(ApiResponse.success("获取设备类型成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("获取设备类型失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新设备类型
     */
    @PutMapping("/{deviceTypeCode}")
    public ResponseEntity<ApiResponse<DeviceTypeResponse>> updateDeviceType(
            @PathVariable String deviceTypeCode, 
            @Valid @RequestBody DeviceTypeRequest request) {
        try {
            DeviceTypeResponse response = deviceTypeService.updateDeviceType(deviceTypeCode, request);
            return ResponseEntity.ok(ApiResponse.success("设备类型更新成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新设备类型失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除设备类型
     */
    @DeleteMapping("/{deviceTypeCode}")
    public ResponseEntity<ApiResponse<Void>> deleteDeviceType(@PathVariable String deviceTypeCode) {
        try {
            deviceTypeService.deleteDeviceType(deviceTypeCode);
            return ResponseEntity.ok(ApiResponse.success("设备类型删除成功",null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除设备类型失败: " + e.getMessage()));
        }
    }
    
    /**
     * 分页查询设备类型
     */
    @GetMapping
    public ResponseEntity<ApiResponse<IPage<DeviceTypeResponse>>> queryDeviceTypes(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String tbTypeCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            DeviceTypeQueryRequest queryRequest = new DeviceTypeQueryRequest();
            queryRequest.setName(name);
            queryRequest.setStatus(status);
            queryRequest.setTbTypeCode(tbTypeCode);
            queryRequest.setPage(page);
            queryRequest.setSize(size);
            queryRequest.setSortBy(sortBy);
            queryRequest.setSortDirection(sortDirection);
            
            IPage<DeviceTypeResponse> response = deviceTypeService.queryDeviceTypes(queryRequest);
            return ResponseEntity.ok(ApiResponse.success("查询设备类型成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询设备类型失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有设备类型
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<DeviceTypeResponse>>> getAllDeviceTypes() {
        try {
            List<DeviceTypeResponse> response = deviceTypeService.getAllDeviceTypes();
            return ResponseEntity.ok(ApiResponse.success("获取所有设备类型成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取所有设备类型失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据状态获取设备类型
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<DeviceTypeResponse>>> getDeviceTypesByStatus(@PathVariable Integer status) {
        try {
            List<DeviceTypeResponse> response = deviceTypeService.getDeviceTypesByStatus(status);
            return ResponseEntity.ok(ApiResponse.success("根据状态获取设备类型成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("根据状态获取设备类型失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据名称搜索设备类型
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DeviceTypeResponse>>> searchDeviceTypesByName(@RequestParam String name) {
        try {
            List<DeviceTypeResponse> response = deviceTypeService.searchDeviceTypesByName(name);
            return ResponseEntity.ok(ApiResponse.success("搜索设备类型成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("搜索设备类型失败: " + e.getMessage()));
        }
    }
    
    /**
     * 统计指定状态的设备类型数量
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countByStatus(@RequestParam Integer status) {
        try {
            long count = deviceTypeService.countByStatus(status);
            return ResponseEntity.ok(ApiResponse.success("统计设备类型数量成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("统计设备类型数量失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量删除设备类型
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchDeleteDeviceTypes(@RequestBody List<String> deviceTypeCodes) {
        try {
            deviceTypeService.batchDeleteDeviceTypes(deviceTypeCodes);
            return ResponseEntity.ok(ApiResponse.success("批量删除设备类型成功",null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("批量删除设备类型失败: " + e.getMessage()));
        }
    }




}

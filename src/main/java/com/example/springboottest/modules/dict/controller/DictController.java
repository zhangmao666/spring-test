package com.example.springboottest.modules.dict.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.dict.dto.*;
import com.example.springboottest.modules.dict.service.DictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dicts")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createDict(@Valid @RequestBody DictRequest request) {
        try {
            Long dictId = dictService.createDict(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("创建字典成功", dictId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("创建字典失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DictResponse>> getDictById(@PathVariable Long id) {
        try {
            DictResponse response = dictService.getDictById(id);
            return ResponseEntity.ok(ApiResponse.success("获取字典成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("获取字典失败: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{dictCode}")
    public ResponseEntity<ApiResponse<DictResponse>> getDictByCode(@PathVariable String dictCode) {
        try {
            DictResponse response = dictService.getDictByCode(dictCode);
            return ResponseEntity.ok(ApiResponse.success("获取字典成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("获取字典失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DictResponse>> updateDict(@PathVariable Long id, @Valid @RequestBody DictRequest request) {
        try {
            DictResponse response = dictService.updateDict(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新字典成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("更新字典失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDict(@PathVariable Long id) {
        try {
            dictService.deleteDict(id);
            return ResponseEntity.ok(ApiResponse.success("删除字典成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("删除字典失败: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<IPage<DictResponse>>> queryDicts(
            @RequestParam(required = false) String dictCode,
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            DictQueryRequest queryRequest = new DictQueryRequest();
            queryRequest.setDictCode(dictCode);
            queryRequest.setDictName(dictName);
            queryRequest.setStatus(status);
            queryRequest.setPage(page);
            queryRequest.setSize(size);
            IPage<DictResponse> response = dictService.queryDicts(queryRequest);
            return ResponseEntity.ok(ApiResponse.success("查询字典成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("查询字典失败: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DictResponse>>> getAllActiveDicts() {
        try {
            List<DictResponse> response = dictService.getAllActiveDicts();
            return ResponseEntity.ok(ApiResponse.success("获取所有启用字典成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取所有启用字典失败: " + e.getMessage()));
        }
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<Long>> createDictItem(@Valid @RequestBody DictItemRequest request) {
        try {
            Long itemId = dictService.createDictItem(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("创建字典项成功", itemId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("创建字典项失败: " + e.getMessage()));
        }
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ApiResponse<DictItemResponse>> getDictItemById(@PathVariable Long id) {
        try {
            DictItemResponse response = dictService.getDictItemById(id);
            return ResponseEntity.ok(ApiResponse.success("获取字典项成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("获取字典项失败: " + e.getMessage()));
        }
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<DictItemResponse>> updateDictItem(@PathVariable Long id, @Valid @RequestBody DictItemRequest request) {
        try {
            DictItemResponse response = dictService.updateDictItem(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新字典项成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("更新字典项失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDictItem(@PathVariable Long id) {
        try {
            dictService.deleteDictItem(id);
            return ResponseEntity.ok(ApiResponse.success("删除字典项成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("删除字典项失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{dictId}/items")
    public ResponseEntity<ApiResponse<List<DictItemResponse>>> getDictItemsByDictId(@PathVariable Long dictId) {
        try {
            List<DictItemResponse> response = dictService.getDictItemsByDictId(dictId);
            return ResponseEntity.ok(ApiResponse.success("获取字典项成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取字典项失败: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{dictCode}/items")
    public ResponseEntity<ApiResponse<List<DictItemResponse>>> getDictItemsByCode(@PathVariable String dictCode) {
        try {
            List<DictItemResponse> response = dictService.getDictItemsByCode(dictCode);
            return ResponseEntity.ok(ApiResponse.success("获取字典项成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取字典项失败: " + e.getMessage()));
        }
    }
}

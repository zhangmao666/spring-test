package com.example.springboottest.controller;

import com.example.springboottest.DTO.ApiResponse;
import com.example.springboottest.util.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * OkHttp 测试控制器
 * 用于测试 OkHttpUtil 工具类的各种功能
 */
@Slf4j
@RestController
@RequestMapping("/okhttp-test")
public class OkHttpTestController {

    /**
     * 测试 GET 请求
     */
    @GetMapping("/test-get")
    public ApiResponse<String> testGet() {
        try {
            // 测试一个公开的API
            String url = "https://api.github.com/users/github";
            String result = OkHttpUtil.get(url);
            return ApiResponse.success("GET请求成功", result);
        } catch (IOException e) {
            log.error("GET请求失败", e);
            return ApiResponse.error(500, "GET请求失败: " + e.getMessage());
        }
    }

    /**
     * 测试带参数的 GET 请求
     */
    @GetMapping("/test-get-with-params")
    public ApiResponse<String> testGetWithParams() {
        try {
            String url = "https://httpbin.org/get";
            Map<String, String> params = new HashMap<>();
            params.put("name", "test");
            params.put("age", "18");
            
            String result = OkHttpUtil.get(url, params);
            return ApiResponse.success("带参数的GET请求成功", result);
        } catch (IOException e) {
            log.error("GET请求失败", e);
            return ApiResponse.error(500, "GET请求失败: " + e.getMessage());
        }
    }

    /**
     * 测试 POST JSON 请求
     */
    @PostMapping("/test-post-json")
    public ApiResponse<String> testPostJson(@RequestBody(required = false) String jsonData) {
        try {
            String url = "https://httpbin.org/post";
            String json = jsonData != null ? jsonData : "{\"name\":\"test\",\"message\":\"Hello OkHttp\"}";
            
            String result = OkHttpUtil.post(url, json);
            return ApiResponse.success("POST JSON请求成功", result);
        } catch (IOException e) {
            log.error("POST请求失败", e);
            return ApiResponse.error(500, "POST请求失败: " + e.getMessage());
        }
    }

    /**
     * 测试 POST 表单请求
     */
    @PostMapping("/test-post-form")
    public ApiResponse<String> testPostForm() {
        try {
            String url = "https://httpbin.org/post";
            Map<String, String> params = new HashMap<>();
            params.put("username", "testuser");
            params.put("password", "testpass");
            
            String result = OkHttpUtil.postForm(url, params);
            return ApiResponse.success("POST表单请求成功", result);
        } catch (IOException e) {
            log.error("POST表单请求失败", e);
            return ApiResponse.error(500, "POST表单请求失败: " + e.getMessage());
        }
    }

    /**
     * 测试带请求头的请求
     */
    @GetMapping("/test-with-headers")
    public ApiResponse<String> testWithHeaders() {
        try {
            String url = "https://httpbin.org/headers";
            Map<String, String> headers = new HashMap<>();
            headers.put("Custom-Header", "CustomValue");
            headers.put("Authorization", "Bearer test-token");
            
            String result = OkHttpUtil.get(url, null, headers);
            return ApiResponse.success("带请求头的GET请求成功", result);
        } catch (IOException e) {
            log.error("请求失败", e);
            return ApiResponse.error(500, "请求失败: " + e.getMessage());
        }
    }

    /**
     * 测试 PUT 请求
     */
    @PutMapping("/test-put")
    public ApiResponse<String> testPut() {
        try {
            String url = "https://httpbin.org/put";
            String json = "{\"id\":1,\"name\":\"updated name\"}";
            
            String result = OkHttpUtil.put(url, json);
            return ApiResponse.success("PUT请求成功", result);
        } catch (IOException e) {
            log.error("PUT请求失败", e);
            return ApiResponse.error(500, "PUT请求失败: " + e.getMessage());
        }
    }

    /**
     * 测试 DELETE 请求
     */
    @DeleteMapping("/test-delete")
    public ApiResponse<String> testDelete() {
        try {
            String url = "https://httpbin.org/delete";
            
            String result = OkHttpUtil.delete(url);
            return ApiResponse.success("DELETE请求成功", result);
        } catch (IOException e) {
            log.error("DELETE请求失败", e);
            return ApiResponse.error(500, "DELETE请求失败: " + e.getMessage());
        }
    }

    /**
     * 测试异步 GET 请求
     */
    @GetMapping("/test-async-get")
    public ApiResponse<String> testAsyncGet() {
        String url = "https://httpbin.org/get";
        
        OkHttpUtil.getAsync(url, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().string();
                    log.info("异步GET请求成功: {}", result);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                log.error("异步GET请求失败", e);
            }
        });

        return ApiResponse.success("异步GET请求已发送，请查看日志");
    }

    /**
     * 测试异步 POST 请求
     */
    @PostMapping("/test-async-post")
    public ApiResponse<String> testAsyncPost() {
        String url = "https://httpbin.org/post";
        String json = "{\"async\":true,\"message\":\"异步POST测试\"}";
        
        OkHttpUtil.postAsync(url, json, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().string();
                    log.info("异步POST请求成功: {}", result);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                log.error("异步POST请求失败", e);
            }
        });

        return ApiResponse.success("异步POST请求已发送，请查看日志");
    }

    /**
     * 测试自定义超时
     */
    @GetMapping("/test-custom-timeout")
    public ApiResponse<String> testCustomTimeout() {
        try {
            String url = "https://httpbin.org/delay/2"; // 延迟2秒响应
            String json = "{}";
            
            // 设置5秒超时
            String result = OkHttpUtil.postWithTimeout(url, json, 5, 5);
            return ApiResponse.success("自定义超时请求成功", result);
        } catch (IOException e) {
            log.error("请求超时", e);
            return ApiResponse.error(500, "请求失败: " + e.getMessage());
        }
    }

    /**
     * 获取工具类使用说明
     */
    @GetMapping("/usage")
    public ApiResponse<Map<String, Object>> getUsage() {
        Map<String, Object> usage = new HashMap<>();
        
        usage.put("description", "OkHttp工具类使用示例");
        
        Map<String, String> examples = new HashMap<>();
        examples.put("GET请求", "OkHttpUtil.get(url)");
        examples.put("GET带参数", "OkHttpUtil.get(url, params)");
        examples.put("GET带请求头", "OkHttpUtil.get(url, params, headers)");
        examples.put("POST JSON", "OkHttpUtil.post(url, json)");
        examples.put("POST表单", "OkHttpUtil.postForm(url, params)");
        examples.put("PUT请求", "OkHttpUtil.put(url, json)");
        examples.put("DELETE请求", "OkHttpUtil.delete(url)");
        examples.put("异步GET", "OkHttpUtil.getAsync(url, callback)");
        examples.put("异步POST", "OkHttpUtil.postAsync(url, json, callback)");
        examples.put("文件上传", "OkHttpUtil.uploadFile(url, file)");
        examples.put("文件下载", "OkHttpUtil.downloadFile(url, savePath)");
        examples.put("自定义超时", "OkHttpUtil.postWithTimeout(url, json, connectTimeout, readTimeout)");
        
        usage.put("methods", examples);
        
        Map<String, String> testEndpoints = new HashMap<>();
        testEndpoints.put("GET测试", "GET /api/okhttp-test/test-get");
        testEndpoints.put("GET带参数测试", "GET /api/okhttp-test/test-get-with-params");
        testEndpoints.put("POST JSON测试", "POST /api/okhttp-test/test-post-json");
        testEndpoints.put("POST表单测试", "POST /api/okhttp-test/test-post-form");
        testEndpoints.put("带请求头测试", "GET /api/okhttp-test/test-with-headers");
        testEndpoints.put("PUT测试", "PUT /api/okhttp-test/test-put");
        testEndpoints.put("DELETE测试", "DELETE /api/okhttp-test/test-delete");
        testEndpoints.put("异步GET测试", "GET /api/okhttp-test/test-async-get");
        testEndpoints.put("异步POST测试", "POST /api/okhttp-test/test-async-post");
        testEndpoints.put("自定义超时测试", "GET /api/okhttp-test/test-custom-timeout");
        
        usage.put("testEndpoints", testEndpoints);
        
        return ApiResponse.success("使用说明", usage);
    }
}


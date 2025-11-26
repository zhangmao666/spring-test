package com.example.springboottest.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 工具类
 * 提供常用的 HTTP 请求方法
 */
@Slf4j
@Component
public class OkHttpUtil {

    @Autowired
    private OkHttpClient okHttpClient;

    private static OkHttpClient client;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType XML = MediaType.parse("application/xml; charset=utf-8");
    public static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    @PostConstruct
    public void init() {
        client = this.okHttpClient;
        log.info("OkHttpUtil 初始化完成");
    }

    /**
     * GET 请求
     *
     * @param url 请求地址
     * @return 响应内容
     */
    public static String get(String url) throws IOException {
        return get(url, null, null);
    }

    /**
     * GET 请求（带参数）
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> params) throws IOException {
        return get(url, params, null);
    }

    /**
     * GET 请求（带参数和请求头）
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        // 构建URL参数
        String fullUrl = buildUrlWithParams(url, params);

        Request.Builder requestBuilder = new Request.Builder().url(fullUrl);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code() + " " + response.message());
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    /**
     * POST 请求（JSON）
     *
     * @param url  请求地址
     * @param json JSON 字符串
     * @return 响应内容
     */
    public static String post(String url, String json) throws IOException {
        return post(url, json, null);
    }

    /**
     * POST 请求（JSON，带请求头）
     *
     * @param url     请求地址
     * @param json    JSON 字符串
     * @param headers 请求头
     * @return 响应内容
     */
    public static String post(String url, String json, Map<String, String> headers) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        return executePost(url, body, headers);
    }

    /**
     * POST 表单请求
     *
     * @param url    请求地址
     * @param params 表单参数
     * @return 响应内容
     */
    public static String postForm(String url, Map<String, String> params) throws IOException {
        return postForm(url, params, null);
    }

    /**
     * POST 表单请求（带请求头）
     *
     * @param url     请求地址
     * @param params  表单参数
     * @param headers 请求头
     * @return 响应内容
     */
    public static String postForm(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null && !params.isEmpty()) {
            params.forEach(formBuilder::add);
        }
        RequestBody body = formBuilder.build();
        return executePost(url, body, headers);
    }

    /**
     * PUT 请求（JSON）
     *
     * @param url  请求地址
     * @param json JSON 字符串
     * @return 响应内容
     */
    public static String put(String url, String json) throws IOException {
        return put(url, json, null);
    }

    /**
     * PUT 请求（JSON，带请求头）
     *
     * @param url     请求地址
     * @param json    JSON 字符串
     * @param headers 请求头
     * @return 响应内容
     */
    public static String put(String url, String json, Map<String, String> headers) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .put(body);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code() + " " + response.message());
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    /**
     * DELETE 请求
     *
     * @param url 请求地址
     * @return 响应内容
     */
    public static String delete(String url) throws IOException {
        return delete(url, null);
    }

    /**
     * DELETE 请求（带请求头）
     *
     * @param url     请求地址
     * @param headers 请求头
     * @return 响应内容
     */
    public static String delete(String url, Map<String, String> headers) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .delete();

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code() + " " + response.message());
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    /**
     * 异步 GET 请求
     *
     * @param url      请求地址
     * @param callback 回调函数
     */
    public static void getAsync(String url, Callback callback) {
        getAsync(url, null, null, callback);
    }

    /**
     * 异步 GET 请求（带参数和请求头）
     *
     * @param url      请求地址
     * @param params   请求参数
     * @param headers  请求头
     * @param callback 回调函数
     */
    public static void getAsync(String url, Map<String, String> params, Map<String, String> headers, Callback callback) {
        String fullUrl = buildUrlWithParams(url, params);

        Request.Builder requestBuilder = new Request.Builder().url(fullUrl);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 异步 POST 请求
     *
     * @param url      请求地址
     * @param json     JSON 字符串
     * @param callback 回调函数
     */
    public static void postAsync(String url, String json, Callback callback) {
        postAsync(url, json, null, callback);
    }

    /**
     * 异步 POST 请求（带请求头）
     *
     * @param url      请求地址
     * @param json     JSON 字符串
     * @param headers  请求头
     * @param callback 回调函数
     */
    public static void postAsync(String url, String json, Map<String, String> headers, Callback callback) {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 上传文件
     *
     * @param url  请求地址
     * @param file 文件
     * @return 响应内容
     */
    public static String uploadFile(String url, File file) throws IOException {
        return uploadFile(url, file, "file", null);
    }

    /**
     * 上传文件（带参数）
     *
     * @param url       请求地址
     * @param file      文件
     * @param fieldName 文件字段名
     * @param params    其他参数
     * @return 响应内容
     */
    public static String uploadFile(String url, File file, String fieldName, Map<String, String> params) throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        // 添加文件
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
        builder.addFormDataPart(fieldName, file.getName(), fileBody);

        // 添加其他参数
        if (params != null && !params.isEmpty()) {
            params.forEach(builder::addFormDataPart);
        }

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("文件上传失败: " + response.code() + " " + response.message());
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    /**
     * 下载文件
     *
     * @param url      请求地址
     * @param savePath 保存路径
     */
    public static void downloadFile(String url, String savePath) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("文件下载失败: " + response.code() + " " + response.message());
            }

            if (response.body() == null) {
                throw new IOException("响应体为空");
            }

            // 保存文件
            try (InputStream inputStream = response.body().byteStream();
                 FileOutputStream outputStream = new FileOutputStream(savePath)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        }
    }

    /**
     * 自定义超时的请求
     *
     * @param url            请求地址
     * @param json           JSON 字符串
     * @param connectTimeout 连接超时（秒）
     * @param readTimeout    读取超时（秒）
     * @return 响应内容
     */
    public static String postWithTimeout(String url, String json, int connectTimeout, int readTimeout) throws IOException {
        OkHttpClient customClient = client.newBuilder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = customClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code() + " " + response.message());
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 执行 POST 请求
     */
    private static String executePost(String url, RequestBody body, Map<String, String> headers) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code() + " " + response.message());
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    /**
     * 构建带参数的 URL
     */
    private static String buildUrlWithParams(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        params.forEach(urlBuilder::addQueryParameter);
        return urlBuilder.build().toString();
    }

    /**
     * 获取 OkHttpClient 实例
     * 用于更复杂的自定义需求
     */
    public static OkHttpClient getClient() {
        return client;
    }
}


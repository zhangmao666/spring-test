package com.example.springboottest.controller;

import com.example.springboottest.DTO.ApiResponse;
import com.example.springboottest.DTO.QrCodeRequest;
import com.example.springboottest.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;

    /**
     * 生成二维码图片 (GET方式)
     *
     * @param content 二维码内容
     * @param width   图片宽度（可选，默认300）
     * @param height  图片高度（可选，默认300）
     * @return 二维码图片
     */
    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateQrCode(
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "300") int width,
            @RequestParam(required = false, defaultValue = "300") int height) {
        
        try {
            log.info("生成二维码: content={}, width={}, height={}", content, width, height);
            
            byte[] qrCodeImage = qrCodeService.generateQrCode(content, width, height);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeImage.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            
            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("生成二维码失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 生成二维码图片 (POST方式)
     *
     * @param request 二维码生成请求
     * @return 二维码图片
     */
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateQrCodePost(@RequestBody QrCodeRequest request) {
        try {
            log.info("生成二维码: content={}, width={}, height={}", request.getContent(), request.getWidth(), request.getHeight());
            
            byte[] qrCodeImage = qrCodeService.generateQrCode(
                request.getContent(), 
                request.getWidth() != null ? request.getWidth() : 300, 
                request.getHeight() != null ? request.getHeight() : 300
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeImage.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            
            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("生成二维码失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 生成二维码Base64编码 (GET方式)
     *
     * @param content 二维码内容
     * @param width   图片宽度（可选，默认300）
     * @param height  图片高度（可选，默认300）
     * @return Base64编码的二维码图片
     */
    @GetMapping("/generate-base64")
    public ApiResponse<Map<String, String>> generateQrCodeBase64(
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "300") int width,
            @RequestParam(required = false, defaultValue = "300") int height) {
        
        try {
            log.info("生成Base64二维码: content={}, width={}, height={}", content, width, height);
            
            byte[] qrCodeImage = qrCodeService.generateQrCode(content, width, height);
            String base64Image = Base64.getEncoder().encodeToString(qrCodeImage);
            
            Map<String, String> result = new HashMap<>();
            result.put("image", "data:image/png;base64," + base64Image);
            result.put("content", content);
            
            return ApiResponse.success("二维码生成成功", result);
        } catch (Exception e) {
            log.error("生成Base64二维码失败: {}", e.getMessage(), e);
            return ApiResponse.error("生成二维码失败: " + e.getMessage());
        }
    }

    /**
     * 生成二维码Base64编码 (POST方式)
     *
     * @param request 二维码生成请求
     * @return Base64编码的二维码图片
     */
    @PostMapping("/generate-base64")
    public ApiResponse<Map<String, String>> generateQrCodeBase64Post(@RequestBody QrCodeRequest request) {
        try {
            log.info("生成Base64二维码: content={}, width={}, height={}", request.getContent(), request.getWidth(), request.getHeight());
            
            byte[] qrCodeImage = qrCodeService.generateQrCode(
                request.getContent(), 
                request.getWidth() != null ? request.getWidth() : 300, 
                request.getHeight() != null ? request.getHeight() : 300
            );
            String base64Image = Base64.getEncoder().encodeToString(qrCodeImage);
            
            Map<String, String> result = new HashMap<>();
            result.put("image", "data:image/png;base64," + base64Image);
            result.put("content", request.getContent());
            
            return ApiResponse.success("二维码生成成功", result);
        } catch (Exception e) {
            log.error("生成Base64二维码失败: {}", e.getMessage(), e);
            return ApiResponse.error("生成二维码失败: " + e.getMessage());
        }
    }
}
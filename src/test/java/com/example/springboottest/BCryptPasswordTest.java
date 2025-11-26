package com.example.springboottest;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt密码测试
 * 用于验证和生成OAuth2客户端密钥
 */
public class BCryptPasswordTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 测试密码 "123456" 的BCrypt hash
     */
    @Test
    public void testVerifyPassword() {
        String rawPassword = "123456";
        String storedHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVajIK";
        
        System.out.println("=== BCrypt密码验证测试 ===");
        System.out.println("明文密码: " + rawPassword);
        System.out.println("存储的Hash: " + storedHash);
        
        boolean matches = passwordEncoder.matches(rawPassword, storedHash);
        
        System.out.println("验证结果: " + (matches ? "✓ 匹配成功" : "✗ 匹配失败"));
        
        if (!matches) {
            System.out.println("\n重新生成正确的Hash:");
            String newHash = passwordEncoder.encode(rawPassword);
            System.out.println("新Hash: " + newHash);
        }
    }

    /**
     * 生成新的BCrypt密码
     */
    @Test
    public void generateNewPassword() {
        String rawPassword = "123456";
        
        System.out.println("=== 生成BCrypt密码 ===");
        System.out.println("明文密码: " + rawPassword);
        
        // 生成5个不同的hash（每次都不同）
        for (int i = 1; i <= 5; i++) {
            String hash = passwordEncoder.encode(rawPassword);
            System.out.println("Hash " + i + ": " + hash);
            
            // 验证生成的hash是否有效
            boolean valid = passwordEncoder.matches(rawPassword, hash);
            System.out.println("  验证: " + (valid ? "✓" : "✗"));
        }
    }

    /**
     * 测试带{bcrypt}前缀的情况
     */
    @Test
    public void testWithPrefix() {
        String rawPassword = "123456";
        String hashWithoutPrefix = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVajIK";
        String hashWithPrefix = "{bcrypt}" + hashWithoutPrefix;
        
        System.out.println("=== 测试{bcrypt}前缀 ===");
        System.out.println("明文密码: " + rawPassword);
        System.out.println("\n1. 不带前缀:");
        System.out.println("   Hash: " + hashWithoutPrefix);
        System.out.println("   匹配: " + passwordEncoder.matches(rawPassword, hashWithoutPrefix));
        
        System.out.println("\n2. 带前缀:");
        System.out.println("   Hash: " + hashWithPrefix);
        
        // BCryptPasswordEncoder不能直接验证带{bcrypt}前缀的
        // Spring Security的DelegatingPasswordEncoder可以
        try {
            boolean matches = passwordEncoder.matches(rawPassword, hashWithPrefix);
            System.out.println("   匹配: " + matches);
        } catch (Exception e) {
            System.out.println("   错误: " + e.getMessage());
        }
    }

    /**
     * 批量生成测试客户端密码
     */
    @Test
    public void generateTestClientPasswords() {
        String[] passwords = {"123456", "secret", "password", "test123"};
        
        System.out.println("=== 生成测试客户端密码 ===\n");
        
        for (String pwd : passwords) {
            String hash = passwordEncoder.encode(pwd);
            System.out.println("明文: " + pwd);
            System.out.println("Hash: " + hash);
            System.out.println("SQL: INSERT INTO api_clients (client_secret) VALUES ('" + hash + "');");
            System.out.println();
        }
    }
}

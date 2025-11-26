package com.example.springboottest.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: zm
 * @Created: 2025/9/29 下午5:21
 * @Description:
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "di")
public class DiProperties {

    public List<String> url;
}
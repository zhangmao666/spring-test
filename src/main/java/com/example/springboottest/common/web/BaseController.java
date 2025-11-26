package com.example.springboottest.common.web;

import com.alibaba.fastjson2.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.util.Date;

/**
 * @Author: zm
 * @Created: 2025/11/5 上午9:56
 * @Description:
 */

public class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @InitBinder
    public void initBinder(WebDataBinder binder){
        // Date类型转换
        binder.registerCustomEditor(Date.class,new PropertyEditorSupport(){

            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                super.setValue(DateUtils.parseDate(text));
            }
        });
    }



}
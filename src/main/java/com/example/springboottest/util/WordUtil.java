package com.example.springboottest.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.InputStream;

/**
 * @Author: zm
 * @Created: 2025/10/14 下午2:26
 * @Description: 文本处理
 */
@Slf4j
public class WordUtil {

    public static String toText(InputStream fis, String fileName) {
        StringBuilder sb = new StringBuilder();
        try {
            String text = "";
            if (fileName.endsWith(".doc")) {
                try (HWPFDocument hwpfDocument = new HWPFDocument(fis);
                     WordExtractor wordExtractor = new WordExtractor(hwpfDocument)) {
                    text = wordExtractor.getText();
                }
            } else if (fileName.endsWith(".docx")) {
                try (XWPFDocument xwpfDocument = new XWPFDocument(fis);
                     XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(xwpfDocument)) {
                    text = xwpfWordExtractor.getText().trim();
                }
            } else {
                sb.append("不支持的文件类型");
            }
        } catch (Exception e) {
            log.error("解析文件失败！", e);
            return "";
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    log.error("关闭文件流失败！", e);
                }
            }
        }
        return sb.toString().replaceAll("(\r?\n)+", "\n");
    }


}
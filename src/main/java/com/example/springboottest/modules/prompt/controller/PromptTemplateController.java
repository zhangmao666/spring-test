package com.example.springboottest.modules.prompt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.prompt.dto.PromptRenderRequest;
import com.example.springboottest.modules.prompt.dto.PromptRenderResponse;
import com.example.springboottest.modules.prompt.dto.PromptTemplateQueryRequest;
import com.example.springboottest.modules.prompt.dto.PromptTemplateRequest;
import com.example.springboottest.modules.prompt.dto.PromptTemplateResponse;
import com.example.springboottest.modules.prompt.service.PromptTemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/prompt-templates")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Tag(name = "Prompt 模板管理", description = "统一管理 Prompt 模板、数据库存储和缓存读取")
public class PromptTemplateController {

    private final PromptTemplateService promptTemplateService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPromptTemplate(@Valid @RequestBody PromptTemplateRequest request) {
        try {
            Long id = promptTemplateService.createPromptTemplate(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Prompt template created", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Create prompt template failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromptTemplateResponse>> getPromptTemplateById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Prompt template fetched", promptTemplateService.getPromptTemplateById(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Get prompt template failed: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{promptCode}")
    public ResponseEntity<ApiResponse<PromptTemplateResponse>> getPromptTemplateByCode(@PathVariable String promptCode) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Prompt template fetched", promptTemplateService.getPromptTemplateByCode(promptCode)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Get prompt template failed: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromptTemplateResponse>> updatePromptTemplate(@PathVariable Long id,
                                                                                    @Valid @RequestBody PromptTemplateRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Prompt template updated", promptTemplateService.updatePromptTemplate(id, request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Update prompt template failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePromptTemplate(@PathVariable Long id) {
        try {
            promptTemplateService.deletePromptTemplate(id);
            return ResponseEntity.ok(ApiResponse.success("Prompt template deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Delete prompt template failed: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<IPage<PromptTemplateResponse>>> queryPromptTemplates(
            @RequestParam(required = false) String promptCode,
            @RequestParam(required = false) String promptName,
            @RequestParam(required = false) String promptType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PromptTemplateQueryRequest request = new PromptTemplateQueryRequest();
            request.setPromptCode(promptCode);
            request.setPromptName(promptName);
            request.setPromptType(promptType);
            request.setStatus(status);
            request.setPage(page);
            request.setSize(size);
            return ResponseEntity.ok(ApiResponse.success("Prompt templates queried", promptTemplateService.queryPromptTemplates(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Query prompt templates failed: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PromptTemplateResponse>>> getAllActivePromptTemplates() {
        try {
            return ResponseEntity.ok(ApiResponse.success("Active prompt templates fetched", promptTemplateService.getAllActivePromptTemplates()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Get active prompt templates failed: " + e.getMessage()));
        }
    }

    @PostMapping("/render")
    public ResponseEntity<ApiResponse<PromptRenderResponse>> renderPrompt(@Valid @RequestBody PromptRenderRequest request) {
        try {
            String rendered = promptTemplateService.renderPrompt(request.getPromptCode(), request.getVariables());
            return ResponseEntity.ok(ApiResponse.success("Prompt rendered", new PromptRenderResponse(request.getPromptCode(), rendered)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Render prompt failed: " + e.getMessage()));
        }
    }
}

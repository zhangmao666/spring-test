package com.example.springboottest.modules.ai.skill.service;

import com.example.springboottest.modules.ai.skill.entity.AiConversationSkill;
import com.example.springboottest.modules.ai.skill.entity.AiSkill;
import com.example.springboottest.modules.ai.skill.dto.AiSkillResponse;
import com.example.springboottest.modules.ai.skill.mapper.AiConversationSkillMapper;
import com.example.springboottest.modules.ai.skill.mapper.AiSkillMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AiSkillServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldRejectMoreThanFiftyMountedSkills() {
        AiSkillServiceImpl service = new AiSkillServiceImpl(null, null, null);

        List<Long> skillIds = new ArrayList<>();
        for (long i = 1; i <= 51; i++) {
            skillIds.add(i);
        }

        assertThrows(IllegalArgumentException.class, () -> service.saveConversationSkills("conv-1", skillIds));
    }

    @Test
    void shouldBuildMountedSkillPromptFromPromptSkills() {
        AiSkillMapper skillMapper = proxy(AiSkillMapper.class, List.of(
                new AiSkill()
                        .setId(1L)
                        .setSkillKey("writer")
                        .setName("写作助手")
                        .setDescription("优化表达")
                        .setSkillType("PROMPT")
                        .setContent("使用清晰中文回答")
                        .setEnabled(true)
                        .setDeleted(false)
        ));
        AiConversationSkillMapper conversationSkillMapper = proxy(AiConversationSkillMapper.class, List.of(
                new AiConversationSkill().setConversationId("conv-1").setSkillId(1L).setSortOrder(0)
        ));
        AiSkillServiceImpl service = new AiSkillServiceImpl(skillMapper, conversationSkillMapper, null);

        String prompt = service.buildMountedSkillPrompt("conv-1");

        org.junit.jupiter.api.Assertions.assertTrue(prompt.contains("写作助手"));
        org.junit.jupiter.api.Assertions.assertTrue(prompt.contains("使用清晰中文回答"));
    }

    @Test
    void shouldImportSkillFromZipPackage() throws Exception {
        List<AiSkill> inserted = new ArrayList<>();
        AiSkillMapper skillMapper = proxy(AiSkillMapper.class, List.of(), inserted);
        AiSkillServiceImpl service = new AiSkillServiceImpl(skillMapper, null, null) {
            @Override
            Path customSkillRoot() {
                return tempDir.resolve("custom-skills");
            }
        };
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "code-review.zip",
                "application/zip",
                skillZip("""
                        ---
                        name: Code Review
                        description: Review Java code
                        ---

                        Please review code carefully.
                        """)
        );

        AiSkillResponse response = service.importSkillZip(file);

        org.junit.jupiter.api.Assertions.assertEquals("Code Review", response.getName());
        org.junit.jupiter.api.Assertions.assertEquals("Review Java code", response.getDescription());
        org.junit.jupiter.api.Assertions.assertEquals("MY", response.getOriginType());
        org.junit.jupiter.api.Assertions.assertEquals("LOCAL", response.getSourceType());
        org.junit.jupiter.api.Assertions.assertEquals(1, inserted.size());
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> mapperType, List<?> selectListResult) {
        return proxy(mapperType, selectListResult, new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> mapperType, List<?> selectListResult, List<AiSkill> inserted) {
        return (T) Proxy.newProxyInstance(
                mapperType.getClassLoader(),
                new Class<?>[]{mapperType},
                (proxy, method, args) -> {
                    if ("selectList".equals(method.getName())) {
                        return selectListResult;
                    }
                    if ("insert".equals(method.getName())) {
                        if (args != null && args.length > 0 && args[0] instanceof AiSkill skill) {
                            skill.setId(1L);
                            inserted.add(skill);
                        }
                        return 1;
                    }
                    return null;
                }
        );
    }

    private byte[] skillZip(String skillContent) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry("code-review/SKILL.md"));
            zipOutputStream.write(skillContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }
        return outputStream.toByteArray();
    }
}

package com.example.springboottest.modules.ai.skill.service;

import com.example.springboottest.modules.ai.skill.dto.SkillTestResponse;
import com.example.springboottest.modules.ai.skill.entity.AiSkill;
import com.example.springboottest.modules.ai.skill.mapper.AiSkillExecutionLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

class SkillScriptExecutorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldRejectEntryCommandWithPathEscape() throws Exception {
        Path scripts = tempDir.resolve("scripts");
        Files.createDirectories(scripts);
        AiSkillExecutionLogMapper logMapper = proxyLogMapper();
        SkillScriptExecutor executor = new SkillScriptExecutor(new ObjectMapper(), logMapper);

        AiSkill skill = new AiSkill()
                .setId(1L)
                .setSkillKey("unsafe")
                .setName("Unsafe")
                .setSkillDir(tempDir.toString())
                .setEntryCommand("..\\unsafe.bat");

        SkillTestResponse response = executor.execute(skill, "conv-1", Map.of());

        assertFalse(response.getSuccess());
    }

    private AiSkillExecutionLogMapper proxyLogMapper() {
        return (AiSkillExecutionLogMapper) Proxy.newProxyInstance(
                AiSkillExecutionLogMapper.class.getClassLoader(),
                new Class<?>[]{AiSkillExecutionLogMapper.class},
                (proxy, method, args) -> "insert".equals(method.getName()) ? 1 : null
        );
    }
}

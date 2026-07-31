package com.example.springboottest.modules.ai.agent.service;

import com.example.springboottest.modules.ai.agent.entity.AgentContextSnapshot;
import com.example.springboottest.modules.ai.agent.mapper.AgentContextSnapshotMapper;
import com.example.springboottest.modules.ai.agent.message.AgentMessage;
import com.example.springboottest.modules.ai.agent.message.AgentRole;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentContextCompactServiceTest {

    @Test
    void shouldCreateSummarySnapshotWhenCompacting() {
        FakeSnapshotMapper mapper = new FakeSnapshotMapper();
        AgentContextCompactService service = new AgentContextCompactService(mapper);

        AgentContextSnapshot snapshot = service.compact(
                "run-1",
                "conv-1",
                List.of(AgentMessage.text(AgentRole.USER, "请分析 Claude Code Agent 架构")),
                "Tool result: useful details",
                "AUTO_COMPACT"
        );

        assertNotNull(snapshot.getSummary());
        assertTrue(snapshot.getSummary().contains("最近消息"));
        assertTrue(snapshot.getEstimatedTokens() > 0);
    }

    private static class FakeSnapshotMapper implements AgentContextSnapshotMapper {
        private final List<AgentContextSnapshot> snapshots = new ArrayList<>();

        @Override
        public int insert(AgentContextSnapshot snapshot) {
            snapshots.add(snapshot);
            return 1;
        }

        @Override
        public AgentContextSnapshot findLatestByRunId(String runId) {
            return snapshots.stream()
                    .filter(snapshot -> runId.equals(snapshot.getRunId()))
                    .reduce((first, second) -> second)
                    .orElse(null);
        }

        @Override
        public int countByRunId(String runId) {
            return (int) snapshots.stream().filter(snapshot -> runId.equals(snapshot.getRunId())).count();
        }
    }
}

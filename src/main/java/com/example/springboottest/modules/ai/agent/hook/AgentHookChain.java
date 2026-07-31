package com.example.springboottest.modules.ai.agent.hook;

import com.example.springboottest.modules.ai.agent.event.AgentEvent;
import com.example.springboottest.modules.ai.agent.runtime.AgentCallContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AgentHookChain {

    private final List<AgentHook> hooks;

    public AgentHookChain(List<AgentHook> hooks) {
        this.hooks = hooks == null ? List.of() : List.copyOf(hooks);
    }

    public void publish(AgentCallContext context, AgentEvent event) {
        for (AgentHook hook : hooks) {
            try {
                hook.onEvent(context, event);
            } catch (Exception e) {
                log.warn("Agent hook failed: {}", hook.getClass().getName(), e);
            }
        }
    }
}

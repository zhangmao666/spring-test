package com.example.springboottest.modules.ai.agent.hook;

import com.example.springboottest.modules.ai.agent.event.AgentEvent;
import com.example.springboottest.modules.ai.agent.runtime.AgentCallContext;

public interface AgentHook {

    default void onEvent(AgentCallContext context, AgentEvent event) {
    }
}

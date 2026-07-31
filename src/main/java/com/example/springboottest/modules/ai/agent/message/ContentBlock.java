package com.example.springboottest.modules.ai.agent.message;

import java.util.Map;

public interface ContentBlock {

    ContentBlockType getType();

    Map<String, Object> getMetadata();
}

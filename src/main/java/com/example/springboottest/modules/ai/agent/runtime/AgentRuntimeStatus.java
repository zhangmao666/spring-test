package com.example.springboottest.modules.ai.agent.runtime;

public final class AgentRuntimeStatus {

    private AgentRuntimeStatus() {
    }

    public static final String RUNNING = "RUNNING";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";
    public static final String CANCELLED = "CANCELLED";
    public static final String WAITING_PERMISSION = "WAITING_PERMISSION";

    public static final String TODO_PENDING = "PENDING";
    public static final String TODO_IN_PROGRESS = "IN_PROGRESS";
    public static final String TODO_COMPLETED = "COMPLETED";
    public static final String TODO_CANCELLED = "CANCELLED";

    public static final String PERMISSION_PENDING = "PENDING";
    public static final String PERMISSION_APPROVED = "APPROVED";
    public static final String PERMISSION_DENIED = "DENIED";
}

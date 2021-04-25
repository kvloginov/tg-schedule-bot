package ru.madbunny.schedule.bot;

import java.util.Collections;
import java.util.Map;

public class ConversationContext {
    private final ConversationState state;
    private final Map<String, Object> attributes;

    public ConversationContext(ConversationState state, Map<String, Object> attributes) {
        this.state = state;
        this.attributes = attributes == null ? Map.of() : Collections.unmodifiableMap(attributes);
    }

    public ConversationContext(ConversationState state) {
        this(state, null);
    }

    public ConversationState getState() {
        return state;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}

package org.smarthome.messages.hvac;

public class RespondUpdateMode {
    public final long requestId;

    public RespondUpdateMode(long requestId) {
        this.requestId = requestId;
    }
}

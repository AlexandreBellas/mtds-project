package org.smarthome.messages.hvac;

import org.smarthome.utils.Mode;

public class RespondMode {
    public final Mode currentMode;
    public final long requestId;

    public RespondMode(long requestId, Mode currentMode) {
        this.requestId = requestId;
        this.currentMode = currentMode;
    }
}

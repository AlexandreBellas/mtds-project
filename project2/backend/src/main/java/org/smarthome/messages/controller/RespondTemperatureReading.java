package org.smarthome.messages.controller;

public class RespondTemperatureReading {
    public final long requestId;

    public RespondTemperatureReading(long requestId) {
        this.requestId = requestId;
    }
}

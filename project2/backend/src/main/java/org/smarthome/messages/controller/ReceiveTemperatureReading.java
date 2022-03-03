package org.smarthome.messages.controller;

import akka.actor.typed.ActorRef;
import org.smarthome.actors.device.Controller;

public class ReceiveTemperatureReading implements Controller.Command {
    public final long requestId;
    public final long temperatureReading;
    public final ActorRef<RespondTemperatureReading> replyTo;

    public ReceiveTemperatureReading(long requestId, long temperatureReading, ActorRef<RespondTemperatureReading> replyTo) {
        this.requestId = requestId;
        this.replyTo = replyTo;
        this.temperatureReading = temperatureReading;
    }
}

package org.smarthome.messages.hvac;

import akka.actor.typed.ActorRef;
import org.smarthome.actors.device.HVAC;

public class RequestMode implements HVAC.Command {
    public final long requestId;
    public final ActorRef<RespondMode> replyTo;

    public RequestMode(long requestId, ActorRef<RespondMode> replyTo) {
        this.requestId = requestId;
        this.replyTo = replyTo;
    }
}

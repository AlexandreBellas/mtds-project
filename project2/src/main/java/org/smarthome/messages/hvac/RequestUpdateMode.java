package org.smarthome.messages.hvac;

import akka.actor.typed.ActorRef;
import org.smarthome.actors.device.HVAC;
import org.smarthome.utils.Mode;

public class RequestUpdateMode implements HVAC.Command {
    public final long requestId;
    public final ActorRef<RespondUpdateMode> replyTo;
    public final Mode newMode;

    public RequestUpdateMode(long requestId, Mode newMode, ActorRef<RespondUpdateMode> actorRef) {
        this.requestId = requestId;
        this.replyTo = actorRef;
        this.newMode = newMode;
    }
}

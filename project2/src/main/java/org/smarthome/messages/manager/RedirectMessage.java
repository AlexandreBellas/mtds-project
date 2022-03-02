package org.smarthome.messages.manager;

import akka.actor.typed.ActorRef;
import org.smarthome.actors.DeviceGroup;
import org.smarthome.actors.DeviceManager;
import org.smarthome.messages.hvac.RespondMode;

import java.util.List;

public class RedirectMessage implements DeviceManager.Command, DeviceGroup.Command {
    public final long requestId;
    public final List<String> message;

    public RedirectMessage(long requestId, List<String> message) {
        this.requestId = requestId;
        this.message = message;
    }
}

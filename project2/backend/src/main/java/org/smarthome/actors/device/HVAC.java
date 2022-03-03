package org.smarthome.actors.device;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.smarthome.actors.DeviceManager;
import org.smarthome.messages.hvac.RequestMode;
import org.smarthome.messages.hvac.RequestUpdateMode;
import org.smarthome.messages.hvac.RespondMode;
import org.smarthome.messages.hvac.RespondUpdateMode;
import org.smarthome.utils.Mode;

public class HVAC extends GenericDevice {

    public HVAC(ActorContext<Command> context, String groupId, String deviceId, ActorRef<DeviceManager.Command> manager) {
        super(context, groupId, deviceId, manager);
        context.getLog().info("Device {} from {} started", deviceId, groupId);
    }

    protected Behavior<Command> onPostStop() {
        getContext().getLog().info("Device {} from {} stopped", deviceId, groupId);
        return Behaviors.stopped();
    }

    private Mode currentMode = Mode.OFF;

    public static Behavior<Command> create(String groupId, String deviceId, ActorRef<DeviceManager.Command> manager) {
        return Behaviors.setup(context -> new HVAC(context, groupId, deviceId, manager));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(RequestUpdateMode.class, this::onRequestUpdateModeMessage)
                .onMessage(RequestMode.class, this::onRequestModeMessage)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<Command> onRequestUpdateModeMessage (RequestUpdateMode m) {
        currentMode = m.newMode;
        getContext().getLog().info("[{}-{}] Mode: {}", groupId, deviceId, currentMode);
        m.replyTo.tell(new RespondUpdateMode(m.requestId));
        return this;
    }

    private Behavior<Command> onRequestModeMessage (RequestMode m) {
        getContext().getLog().info("[{}-{}] Mode: {}", groupId, deviceId, currentMode);
        m.replyTo.tell(new RespondMode(m.requestId, currentMode));
        return this;
    }

}

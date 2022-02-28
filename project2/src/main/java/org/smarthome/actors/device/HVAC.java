package org.smarthome.actors.device;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import org.smarthome.messages.hvac.RequestMode;
import org.smarthome.messages.hvac.RequestUpdateMode;
import org.smarthome.messages.hvac.RespondMode;
import org.smarthome.messages.hvac.RespondUpdateMode;
import org.smarthome.utils.Mode;

public class HVAC extends AbstractBehavior<HVAC.Command> {

    public interface Command { }

    public HVAC(ActorContext<Command> context) {
        super(context);
        context.getLog().info("HVAC Device started");
    }

    protected Behavior<Command> onPostStop() {
        getContext().getLog().info("HVAC stopped");
        return Behaviors.stopped();
    }

    private Mode currentMode = Mode.OFF;

    public static Behavior<Command> create() {
        return Behaviors.setup(HVAC::new);
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
        getContext().getLog().info("Mode: {}", currentMode);
        m.replyTo.tell(new RespondUpdateMode(m.requestId));
        return this;
    }

    private Behavior<Command> onRequestModeMessage (RequestMode m) {
        getContext().getLog().info("Mode: {}", currentMode);
        m.replyTo.tell(new RespondMode(m.requestId, currentMode));
        return this;
    }

}

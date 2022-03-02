package org.smarthome.actors.device;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;

public abstract class GenericDevice extends AbstractBehavior<GenericDevice.Command> {

    public abstract interface Command {}

    public GenericDevice(ActorContext<Command> context) {
        super(context);
    }
}

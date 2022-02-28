package org.smarthome.actors.device;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.smarthome.messages.hvac.RequestUpdateMode;
import org.smarthome.messages.controller.ReceiveTemperatureReading;
import org.smarthome.messages.controller.RespondTemperatureReading;
import org.smarthome.utils.Mode;

public class Controller extends AbstractBehavior<Controller.Command> {

    public interface Command { }
    public final ActorRef<HVAC.Command> hvacRef;

    public long getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(long targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    private long targetTemperature = 25;

    public Controller(ActorContext<Command> context, ActorRef<HVAC.Command> hvacRef) {
        super(context);
        this.hvacRef = hvacRef;
        context.getLog().info("Controller Device started");
    }

    public static Behavior<Command> create(ActorRef<HVAC.Command> hvacRef) {
        return Behaviors.setup(context -> new Controller(context, hvacRef));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReceiveTemperatureReading.class, this::onReceiveTemperature)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    protected Behavior<Command> onPostStop() {
        getContext().getLog().info("HVAC stopped");
        return Behaviors.stopped();
    }

    private Behavior<Command> onReceiveTemperature (ReceiveTemperatureReading t) {
        getContext().getLog().info("Temperature Received: {}", t.temperatureReading);
        Mode nextMode = nextMode(t.temperatureReading);
        hvacRef.tell(new RequestUpdateMode(2830, nextMode, getContext()
                .getSelf()
                .unsafeUpcast()));
        t.replyTo.tell(new RespondTemperatureReading(34763));
        return this;
    }

    public Mode nextMode(long temperatureReading) {
        Mode mode;
        if (temperatureReading < (targetTemperature - 1))
            mode = Mode.HEATING;
        else if(temperatureReading > (targetTemperature + 1))
            mode = Mode.AIR_CONDITIONING;
        else if( temperatureReading == targetTemperature)
            mode = Mode.OFF;
        else
            mode = Mode.VENTILATION;

        return mode;
    }


}

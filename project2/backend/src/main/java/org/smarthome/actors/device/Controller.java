package org.smarthome.actors.device;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.smarthome.actors.DeviceManager;
import org.smarthome.messages.controller.ReceiveTemperatureReading;
import org.smarthome.messages.controller.RespondTemperatureReading;
import org.smarthome.messages.hvac.RequestUpdateMode;
import org.smarthome.utils.Mode;

import java.util.Map;

import static java.lang.Thread.sleep;

public class Controller extends GenericDevice {

    public long getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(long targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    private long targetTemperature = 25;
    private ActorRef<GenericDevice.Command> hvacRef;

    public Controller(ActorContext<Command> context, String groupId, String deviceId, ActorRef<DeviceManager.Command> manager) {
        super(context, groupId, deviceId, manager);
        context.getLog().info("Controller Device started");
    }

    public static Behavior<Command> create(String groupId, String deviceId, ActorRef<DeviceManager.Command> manager) {
        return Behaviors.setup(context -> new Controller(context, groupId, deviceId, manager));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReceiveTemperatureReading.class, this::onReceiveTemperature)
                .onMessage(DeviceManager.ReplyDeviceList.class, this::onReceiveRegisteredHVAC)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    protected Behavior<Command> onPostStop() {
        getContext().getLog().info("HVAC stopped");
        return Behaviors.stopped();
    }

    private ReceiveTemperatureReading temporaryTemperatureReading;
    private Behavior<Command> onReceiveTemperature (ReceiveTemperatureReading t) {
        getContext().getLog().info("Temperature Received: {}", t.temperatureReading);
        Mode nextMode = nextMode(t.temperatureReading);

        if(hvacRef == null) {
            AskRegisteredHVAC();
            temporaryTemperatureReading = t;
            return this;
        }
        hvacRef.tell(new RequestUpdateMode(2830, nextMode, getContext()
                .getSelf()
                .unsafeUpcast()));
        t.replyTo.tell(new RespondTemperatureReading(34763));
        return this;
    }

    private void AskRegisteredHVAC() {
        manager.tell(new DeviceManager.RequestDeviceList(1, this.groupId, getContext().getSelf().unsafeUpcast()));
    }

    private Behavior<Command> onReceiveRegisteredHVAC(DeviceManager.ReplyDeviceList t) {
        Map<String, ActorRef<Command>> list = t.list;
        hvacRef = list.get("hvac-" + this.code);
        onReceiveTemperature(temporaryTemperatureReading);
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

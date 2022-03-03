package org.smarthome.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.smarthome.actors.device.GenericDevice;
import org.smarthome.messages.controller.ReceiveTemperatureReading;
import org.smarthome.messages.manager.RedirectMessage;

import java.util.HashMap;
import java.util.Map;

// #device-group-full
// #device-group-remove
// #device-group-register
public class DeviceGroup extends AbstractBehavior<DeviceGroup.Command> {

    public interface Command {}

    // #device-terminated
    private static class DeviceTerminated implements Command {
        public final ActorRef<GenericDevice.Command> device;
        public final String groupId;
        public final String deviceId;

        DeviceTerminated(ActorRef<GenericDevice.Command> device, String groupId, String deviceId) {
            this.device = device;
            this.groupId = groupId;
            this.deviceId = deviceId;
        }
    }
    // #device-terminated

    public static Behavior<Command> create(String groupId) {
        return Behaviors.setup(context -> new DeviceGroup(context, groupId));
    }

    private final String groupId;
    private final Map<String, ActorRef<GenericDevice.Command>> deviceIdToActor = new HashMap<>();

    private DeviceGroup(ActorContext<Command> context, String groupId) {
        super(context);
        this.groupId = groupId;
        context.getLog().info("DeviceGroup {} started", groupId);
    }

    private <T extends GenericDevice> DeviceGroup onTrackDevice(DeviceManager.RequestTrackDevice<T.Command> trackMsg) {
        if (this.groupId.equals(trackMsg.groupId)) {
            ActorRef<T.Command> deviceActor = deviceIdToActor.get(trackMsg.deviceId);
            if (deviceActor != null) {
                trackMsg.replyTo.tell(new DeviceManager.DeviceRegistered(deviceActor));
            } else{
                getContext().getLog().info("Creating device actor for {}", trackMsg.deviceId);
                deviceActor = getContext()
                        .spawn(trackMsg.deviceToTrack, "device-" + trackMsg.deviceId);
                // #device-group-register
                getContext()
                        .watchWith(deviceActor, new DeviceTerminated(deviceActor, groupId, trackMsg.deviceId));
                // #device-group-register
                deviceIdToActor.put(trackMsg.deviceId, deviceActor);
                trackMsg.replyTo.tell(new DeviceManager.DeviceRegistered(deviceActor));
            }
        }
        else {
            getContext()
                    .getLog()
                    .warn(
                            "Ignoring TrackDevice request for {}. This actor is responsible for {}.",
                            groupId,
                            this.groupId);
        }
        return this;
    }

    // #device-group-register
    // #device-group-remove

    private DeviceGroup onDeviceList(DeviceManager.RequestDeviceList r) {
        r.replyTo.tell(new DeviceManager.ReplyDeviceList(r.requestId, deviceIdToActor));
        return this;
    }
    // #device-group-remove

    private DeviceGroup onTerminated(DeviceTerminated t) {
        getContext().getLog().info("GenericDevice actor for {} has been terminated", t.deviceId);
        deviceIdToActor.remove(t.deviceId);
        return this;
    }
    // #device-group-register

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(DeviceManager.RequestTrackDevice.class, this::onTrackDevice)
                // #device-group-register
                // #device-group-remove
                .onMessage(
                        DeviceManager.RequestDeviceList.class,
                        r -> r.groupId.equals(groupId),
                        this::onDeviceList)
                // #device-group-remove
                .onMessage(DeviceTerminated.class, this::onTerminated)
                .onMessage(RedirectMessage.class, this::onRedirectMessage)
                .onSignal(PostStop.class, signal -> onPostStop())
                // #device-group-register
                .build();
    }

    private DeviceGroup onRedirectMessage(RedirectMessage m) {
        var deviceName = "controller-" + m.message.get(2);
        var deviceRef = deviceIdToActor.get(deviceName);
        var readValue = Integer.parseInt(m.message.get(3));

        switch (m.message.get(0)){
            case "temperature":
                deviceRef.tell(new ReceiveTemperatureReading(m.requestId, readValue, getContext().getSelf().unsafeUpcast()));
                break;
            default:
                break;
        }
        return this;
    }

    private DeviceGroup onPostStop() {
        getContext().getLog().info("DeviceGroup {} stopped", groupId);
        return this;
    }
}
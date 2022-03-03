package org.smarthome.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.smarthome.actors.device.GenericDevice;
import org.smarthome.messages.manager.RedirectMessage;

import java.util.HashMap;
import java.util.Map;

// #device-manager-full
// #device-registration-msgs
public class DeviceManager extends AbstractBehavior<DeviceManager.Command> {

    public interface Command {}

    public static final class RequestTrackDevice<T extends GenericDevice.Command>
            implements Command, DeviceGroup.Command {
        public final String groupId;
        public final String deviceId;
        public final Behavior<T> deviceToTrack;
        public final ActorRef<DeviceRegistered> replyTo;

        public RequestTrackDevice(String groupId,
                                  String deviceId,
                                  Behavior<T> deviceToTrack,
                                  ActorRef<DeviceRegistered> replyTo)
        {
            this.groupId = groupId;
            this.deviceId = deviceId;
            this.deviceToTrack = deviceToTrack;
            this.replyTo = replyTo;
        }
    }

    public static final class DeviceRegistered {
        public final ActorRef<GenericDevice.Command> device;

        public DeviceRegistered(ActorRef<GenericDevice.Command> device) {
            this.device = device;
        }
    }
    // #device-registration-msgs

    // #device-list-msgs
    public static final class RequestDeviceList
            implements Command, DeviceGroup.Command {
        final long requestId;
        final String groupId;
        final ActorRef<ReplyDeviceList> replyTo;

        public RequestDeviceList(long requestId, String groupId, ActorRef<ReplyDeviceList> replyTo) {
            this.requestId = requestId;
            this.groupId = groupId;
            this.replyTo = replyTo;
        }
    }

    public static final class ReplyDeviceList implements GenericDevice.Command, Command {
        public final long requestId;
        public final Map<String, ActorRef<GenericDevice.Command>> list;

        public ReplyDeviceList(long requestId, Map<String, ActorRef<GenericDevice.Command>> list) {
            this.requestId = requestId;
            this.list = list;
        }
    }

    // #device-list-msgs

    private static class DeviceGroupTerminated implements Command {
        public final String groupId;

        DeviceGroupTerminated(String groupId) {
            this.groupId = groupId;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(DeviceManager::new);
    }

    private final Map<String, ActorRef<DeviceGroup.Command>> groupIdToActor = new HashMap<>();

    private DeviceManager(ActorContext<Command> context) {
        super(context);
        context.getLog().info("DeviceManager started");
    }

    private <T extends GenericDevice.Command> DeviceManager onTrackDevice(RequestTrackDevice<T> trackMsg) {
        String groupId = trackMsg.groupId;
        ActorRef<DeviceGroup.Command> ref = groupIdToActor.get(groupId);
        if (ref != null) {
            ref.tell(trackMsg);
        } else {
            getContext().getLog().info("Creating device group actor for {}", groupId);
            ActorRef<DeviceGroup.Command> groupActor =
                    getContext().spawn(DeviceGroup.create(groupId), "group-" + groupId);
            getContext().watchWith(groupActor, new DeviceGroupTerminated(groupId));
            groupActor.tell(trackMsg);
            groupIdToActor.put(groupId, groupActor);
        }
        return this;
    }

    private DeviceManager onRequestDeviceList(RequestDeviceList request) {
        ActorRef<DeviceGroup.Command> ref = groupIdToActor.get(request.groupId);
        if (ref != null) {
            ref.tell(request);
        } else {
            request.replyTo.tell(new ReplyDeviceList(request.requestId, new HashMap<>()));
        }
        return this;
    }

    private DeviceManager onTerminated(DeviceGroupTerminated t) {
        getContext().getLog().info("Device group actor for {} has been terminated", t.groupId);
        groupIdToActor.remove(t.groupId);
        return this;
    }

    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(RequestTrackDevice.class, this::onTrackDevice)
                .onMessage(RequestDeviceList.class, this::onRequestDeviceList)
                .onMessage(DeviceGroupTerminated.class, this::onTerminated)
                .onMessage(RedirectMessage.class, this::onRedirectMessage)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<Command> onRedirectMessage(RedirectMessage m) {
        var groupName = m.message.get(1) + "-" + m.message.get(2);
        var groupRef = groupIdToActor.get(groupName);
        groupRef.tell(m);
        return this;
    }

    private DeviceManager onPostStop() {
        getContext().getLog().info("DeviceManager stopped");
        return this;
    }
    // #device-registration-msgs






}
package org.smarthome.actors.device;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import org.smarthome.actors.DeviceManager;

public abstract class GenericDevice extends AbstractBehavior<GenericDevice.Command> {

    public interface Command {}

    public String getGroupId() {
        return groupId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    protected final String groupId;
    protected final String deviceId;
    protected final String code;
    protected final ActorRef<DeviceManager.Command> manager;


    public GenericDevice(ActorContext<Command> context, String groupId, String deviceId, ActorRef<DeviceManager.Command> manager) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.manager = manager;

        this.code = deviceId.split("-")[1];
    }
}

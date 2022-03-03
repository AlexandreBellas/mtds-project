package org.smarthome.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import org.smarthome.actors.device.Controller;
import org.smarthome.actors.device.HVAC;

public class IotMain {

    public static void main(String[] args) {
        // Create ActorSystem and top level supervisor
        ActorSystem<Void> IotSystem = ActorSystem.create(IotSupervisor.create(), "iot-system");
        ActorRef<DeviceManager.Command> manager = IotSystem.systemActorOf(
                DeviceManager.create(),
                "device-manager",
                Props.empty()
        );
        ActorRef<MessageBroker.Command> messageBroker = IotSystem.systemActorOf(
                MessageBroker.create(manager),
                "message-broker",
                Props.empty()
        );

        final String[] groupIds = {"bedroom-0001", "bedroom-0002", "kitchen-0001", "livingRoom-0001"};
        String[] deviceId = {"hvac", "controller"};

        for (String groupId : groupIds) {

            var code = groupId.split("-")[1];
            var hvacId = deviceId[0] + "-" + code;
            var controllerId = deviceId[1] + "-" + code;


            var hvacDevice = HVAC.create(groupId, hvacId, manager);
            manager.tell(new DeviceManager.RequestTrackDevice<>(
                    groupId,
                    hvacId,
                    hvacDevice,
                    IotSystem.unsafeUpcast()
            ));

            var controllerDevice = Controller.create(groupId, controllerId, manager);
            manager.tell(new DeviceManager.RequestTrackDevice<>(
                    groupId,
                    controllerId,
                    controllerDevice,
                    IotSystem.unsafeUpcast()
            ));
        }

        messageBroker.tell(new MessageBroker.Activate());




    }
}
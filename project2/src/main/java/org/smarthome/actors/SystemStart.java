package org.smarthome.actors;


import akka.actor.typed.ActorSystem;
import org.smarthome.actors.device.Controller;
import org.smarthome.actors.device.HVAC;
import org.smarthome.messages.controller.ReceiveTemperatureReading;

import java.util.Scanner;

public class SystemStart {
    public static void main(String[] args) {
        //#actor-system

        final String groupId = "bedroom#0001";
        final String deviceId = "hvac#0001";

        final ActorSystem<HVAC.Command> hvacMain = ActorSystem.create(HVAC.create(), "hvac");
        final ActorSystem<Controller.Command> controller = ActorSystem.create(Controller.create(hvacMain), "controller");

        //#actor-system
        try {
            while (true) {
                System.out.println(">>> New Temperature Reading <<<");
                Scanner in = new Scanner(System.in);
                int temp = in.nextInt();
                controller.tell(new ReceiveTemperatureReading(65784, temp, controller.unsafeUpcast()));
            }

        } finally {
            hvacMain.terminate();
        }
    }
}
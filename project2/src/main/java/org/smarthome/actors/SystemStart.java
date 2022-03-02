package org.smarthome.actors;


import akka.Done;
import akka.actor.typed.ActorSystem;

import akka.japi.Pair;
import akka.stream.alpakka.mqtt.MqttConnectionSettings;
import akka.stream.alpakka.mqtt.MqttMessage;
import akka.stream.alpakka.mqtt.MqttQoS;
import akka.stream.alpakka.mqtt.MqttSubscriptions;
import akka.stream.alpakka.mqtt.javadsl.MqttSource;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.smarthome.actors.device.Controller;
import org.smarthome.actors.device.HVAC;
import org.smarthome.messages.controller.ReceiveTemperatureReading;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class SystemStart {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        //#actor-system

        final String groupId = "bedroom#0001";
        final String deviceId = "hvac#0001";

        final ActorSystem<HVAC.Command> hvacMain = ActorSystem.create(HVAC.create(), "hvac");
        final ActorSystem<Controller.Command> controller = ActorSystem.create(Controller.create(hvacMain), "controller");

        final akka.actor.ActorSystem system = akka.actor.ActorSystem.create("MqttSourceTest");

        final int bufferSize = 8;
        final int messageCount = 7;
        final String topic = "test/topic";

        final MqttConnectionSettings connectionSettings =
                MqttConnectionSettings.create(
                        "tcp://broker.hivemq.com:1883", "test-435643236bgfds", new MemoryPersistence()).withAutomaticReconnect(true);
        MqttSubscriptions subscriptions =
                MqttSubscriptions.create(topic, MqttQoS.atMostOnce());

        Source<MqttMessage, CompletionStage<Done>> mqttSource =
                MqttSource.atMostOnce(
                        connectionSettings.withClientId("test-435643236bgfds"), subscriptions, bufferSize);

        try {
            while (true) {
                Pair<CompletionStage<Done>, CompletionStage<List<String>>> materialized =
                        mqttSource
                                .map(m -> m.payload().utf8String())
                                .take(1)
                                .toMat(Sink.seq(), Keep.both())
                                .run(system);

                CompletionStage<Done> subscribed = materialized.first();
                CompletionStage<List<String>> streamResult = materialized.second();

                String result = streamResult.toCompletableFuture().get().get(0);
                System.out.println(result);

                //#actor-system

                int temp = Integer.parseInt(result);
                controller.tell(new ReceiveTemperatureReading(65784, temp, controller.unsafeUpcast()));
            }

        } finally {
            hvacMain.terminate();
        }
    }
}
package org.smarthome.actors;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
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
import org.smarthome.messages.manager.RedirectMessage;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;



public class MessageBroker extends AbstractBehavior<MessageBroker.Command> {

    String brokerUrl = "tcp://localhost:1883";
    String clientId = "test-435643236bgfds";

    public interface Command {}
    ActorRef<DeviceManager.Command> manager;

    public static final class Activate implements Command {}

    public MessageBroker(ActorContext<Command> context, ActorRef<DeviceManager.Command> manager) {
        super(context);
        this.manager = manager;
    }

    public static Behavior<MessageBroker.Command> create(ActorRef<DeviceManager.Command> manager) {
        return Behaviors.setup(context -> new MessageBroker(context, manager));
    }

    @Override
    public Receive<Command> createReceive() {

        return newReceiveBuilder()
                .onMessage(Activate.class, this::onActivateRequest)
                .build();
    }

    public MessageBroker onActivateRequest(Activate a) throws ExecutionException, InterruptedException {
        broker();
        return this;
    }

    public void broker() throws ExecutionException, InterruptedException {
        final akka.actor.ActorSystem system = akka.actor.ActorSystem.create("MqttSourceTest");

        int requestId = 1;

        final int bufferSize = 8;
        final int messageCount = 7;
        final String topic = "temperature/#";

        final MqttConnectionSettings connectionSettings = MqttConnectionSettings.create(brokerUrl, clientId, new MemoryPersistence()).withAutomaticReconnect(true);
        MqttSubscriptions subscriptions = MqttSubscriptions.create(topic, MqttQoS.atMostOnce());

        Source<MqttMessage, CompletionStage<Done>> mqttSource =
                MqttSource.atMostOnce(connectionSettings, subscriptions, bufferSize);

        while (true) {
            Pair<CompletionStage<Done>, CompletionStage<List<List<String>>>> materialized =
                    mqttSource
                            .map(m -> {
                                var val = m.topic().split("/");
                                var payload = m.payload().utf8String();
                                var list = List.of(val[0], val[1], val[2], payload);
                                return list;
                            })
                            .take(1)
                            .toMat(Sink.seq(), Keep.both())
                            .run(system);

            var subscribed = materialized.first();
            var streamResult = materialized.second();

            var result = streamResult.toCompletableFuture().get().get(0);
            System.out.println(result);

            manager.tell(new RedirectMessage(requestId, result));

            requestId +=1;
        }
    }

    }


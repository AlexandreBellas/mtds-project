package org.smarthome.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class IotSupervisor extends AbstractBehavior<Void> {

    public static Behavior<Void> create() {
        return Behaviors.setup(IotSupervisor::new);
    }

    private IotSupervisor(ActorContext<Void> context) {
        super(context);
        context.getLog().info("IoT Application started");
    }

    // No need to handle any messages
    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder()
                .onSignal(PostStop.class, signal -> onPostStop()).build();
    }

//    private IotSupervisor onActivateMessageBroker(ActivateMessage a) throws ExecutionException, InterruptedException {
//
//        final akka.actor.ActorSystem system = akka.actor.ActorSystem.create("MqttSourceTest");
//
//        int requestId = 1;
//
//        final int bufferSize = 8;
//        final int messageCount = 7;
//        final String topic = "temperature/*";
//
//        final MqttConnectionSettings connectionSettings =
//                MqttConnectionSettings.create(
//                        "tcp://broker.hivemq.com:1883", "test-435643236bgfds", new MemoryPersistence()).withAutomaticReconnect(true);
//        MqttSubscriptions subscriptions =
//                MqttSubscriptions.create(topic, MqttQoS.atMostOnce());
//
//        Source<MqttMessage, CompletionStage<Done>> mqttSource =
//                MqttSource.atMostOnce(
//                        connectionSettings.withClientId("test-435643236bgfds"), subscriptions, bufferSize);
//
//        while (true) {
//            Pair<CompletionStage<Done>, CompletionStage<List<List<String>>>> materialized =
//                    mqttSource
//                            .map(m -> List.of(m.topic().split("/")[1], m.payload().utf8String()))
//                            .take(1)
//                            .toMat(Sink.seq(), Keep.both())
//                            .run(system);
//
//            var subscribed = materialized.first();
//            var streamResult = materialized.second();
//
//            var result = streamResult.toCompletableFuture().get().get(0);
//            System.out.println(result);
//
//
//
//            //#actor-system
//            if(getContext().getChild("device-manager").isPresent()){
//                var child = getContext();
////                child.tell(new RedirectMessage(requestId, result));
//            }
//
//            requestId +=1;
//
//
//        }
//    }

    private IotSupervisor onPostStop() {
        getContext().getLog().info("IoT Application stopped");
        return this;
    }
}
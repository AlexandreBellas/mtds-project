package org.smarthome.actors;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.*;
import org.smarthome.actors.device.Controller;
import org.smarthome.actors.device.HVAC;
import org.smarthome.messages.hvac.RequestMode;
import org.smarthome.messages.hvac.RequestUpdateMode;
import org.smarthome.messages.hvac.RespondMode;
import org.smarthome.messages.hvac.RespondUpdateMode;
import org.smarthome.messages.controller.ReceiveTemperatureReading;
import org.smarthome.messages.controller.RespondTemperatureReading;
import org.smarthome.utils.Mode;

import static org.junit.Assert.assertEquals;


//#definition
public class SystemStartTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();
//#definition

    static ActorRef<HVAC.Command> underTest;
    static ActorRef<Controller.Command> controllerTest;

    @BeforeClass
    public static void initialize() {
        underTest = testKit.spawn(HVAC.create(), "hvac");
        controllerTest = testKit.spawn(Controller.create(underTest));
    }

    //#test
    @Test
    public void testUpdateMode() {
        TestProbe<RespondUpdateMode> testProbe = testKit.createTestProbe();
        underTest.tell(new RequestUpdateMode(3560, Mode.AIR_CONDITIONING, testProbe.getRef()));
        RespondUpdateMode response = testProbe.receiveMessage();
        assertEquals(3560, response.requestId);
    }
    //#test

    @Test
    public void testMode() {
        TestProbe<RespondUpdateMode> tempProbe = testKit.createTestProbe();
        underTest.tell(new RequestUpdateMode(3562, Mode.AIR_CONDITIONING, tempProbe.getRef()));

        TestProbe<RespondMode> testProbe = testKit.createTestProbe();
        underTest.tell(new RequestMode(3563, testProbe.getRef()));
        RespondMode response = testProbe.receiveMessage();
        assertEquals(Mode.AIR_CONDITIONING, response.currentMode);
    }

    @Test
    public void ControllerTest() {
        TestProbe<RespondTemperatureReading> testProbe = testKit.createTestProbe();
        controllerTest.tell(new ReceiveTemperatureReading(4356, 28, testProbe.getRef()));
        RespondTemperatureReading res = testProbe.receiveMessage();

        TestProbe<RespondMode> modeProbe = testKit.createTestProbe();
        underTest.tell(new RequestMode(3563, modeProbe.getRef()));
        RespondMode response = modeProbe.receiveMessage();
        assertEquals(Mode.AIR_CONDITIONING, response.currentMode);
    }
}

// Pl1.java
package my.company;

import com.ttsnetwork.modules.standard.*;
import com.ttsnetwork.modulespack.conveyors.*;

public class pl1 extends StateMachine {

    private IRobotCommands r1;
    private IShuttle sh;
    private IConveyorCommands c1c, c1d;
    private ISensorProvider c1cS, c1dS;

    private final SimpleStateVar boxOnC1C = new SimpleStateVar();
    private final SimpleStateVar baseCard1 = new SimpleStateVar();
    private final SimpleStateVar baseCard2 = new SimpleStateVar();
    private final SimpleStateVar cycleDone = new SimpleStateVar();
    private final SimpleStateVar rfidVar = new SimpleStateVar();

    private ConveyorBox boxC;
    private ConveyorBox boxD;
    private ConveyorBox base;

    private String curRfid;

    @Override
    public void onInit() {
        c1c = useSkill(IConveyorCommands.class, "C1C");
        c1cS = useSkill(ISensorProvider.class, "C1C");
        c1cS.registerOnSensors(this::onC1C, "s1");
        c1d = useSkill(IConveyorCommands.class, "C1D");
        c1dS = useSkill(ISensorProvider.class, "C1D");
        c1cS.registerOnSensors(this::onC1C, "s1");

        sh = useSkill(IShuttle.class, "SH1");
        sh.registerOnPosition(2, this::onShAtPos2);

        r1 = useSkill(IRobotCommands.class, "R1");
    }

    @Override
    public void onStart() {
        cycleDone.write(false);
        switchState(100);
    }

    public void state_100() {
        if (boxOnC1C.read() != null
                && baseCard1.read() != null
                && rfidVar.read() != null) {

            boxC = boxOnC1C.readAndForget();
            base = baseCard1.readAndForget();
            curRfid = (String) rfidVar.readAndForget();

            switchState(1000);
        } else if (cycleDone.readBoolean()) {
            switchState(200);
        }
    }

    public void state_200() {
        schedule.startSerial();
        sh.shuttle();
        setVar(cycleDone, false);
        schedule.end();
        switchState(210);
    }

    public void state_210() {
        if (baseCard1.read() != null) {
            switchState(100);
        }
    }

    public void state_1000() {
        if ("P001".equals(curRfid)) {
            onCycleA();
        } else if ("P002".equals(curRfid)) {
            onCycleB();
        }
        switchState(1100);
    }

    public void state_1100() {
        if (cycleDone.readBoolean()) {
            switchState(100);
        }
    }

    private void onC1C(SensorCatch sc) {
        schedule.startSerial();
        c1c.lock(sc.box);
        setVar(boxOnC1C, sc.box);
        schedule.end();
    }

    private void onShAtPos2(SensorCatch t) {
        String rfid = t.box.entity.getProperty(String.class, "rfid");
        setVar(rfidVar, rfid);
        schedule.startSerial();
        setVar(baseCard1, t.box);
        schedule.end();
    }

    private void onCycleA() {
        schedule.startSerial();
        r1.move(driver.getFrameTransform("Frames.f3_1"), 1000);
        r1.move(driver.getFrameTransform("Frames.f4"), 1000);
        r1.pick(boxC.entity);
        c1c.remove(boxC);
        r1.move(driver.getFrameTransform("Frames.f3_1"), 1000);
        r1.move(driver.getFrameTransform("Frames.f3"), 1000);
        r1.release();
        schedule.attach(boxC.entity, base.entity);
        r1.home();
        setVar(cycleDone, true);
        schedule.end();
    }

    private void onCycleB() {
        schedule.startSerial();
        r1.move(driver.getFrameTransform("Frames.f3_1"), 1000);
        r1.move(driver.getFrameTransform("Frames.f4"), 1000);
        r1.pick(boxC.entity);
        c1c.remove(boxC);
        r1.move(driver.getFrameTransform("Frames.f3_1"), 1000);
        r1.move(driver.getFrameTransform("Frames.f3"), 1000);
        r1.release();
        schedule.attach(boxC.entity, base.entity);
        r1.home();
        setVar(cycleDone, true);
        schedule.end();
    }
}

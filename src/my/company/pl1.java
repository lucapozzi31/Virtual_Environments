// Pl1.java
package my.company;

import com.ttsnetwork.modules.standard.IConveyorCommands;
import com.ttsnetwork.modules.standard.IRobotCommands;
import com.ttsnetwork.modules.standard.ISensorProvider;
import com.ttsnetwork.modules.standard.IShuttle;
import com.ttsnetwork.modules.standard.StateMachine;
import com.ttsnetwork.modules.standard.SimpleStateVar;
import com.ttsnetwork.modulespack.conveyors.ConveyorBox;
import com.ttsnetwork.modulespack.conveyors.SensorCatch;

public class pl1 extends StateMachine {

    private IRobotCommands r1;
    private IShuttle sh;
    private IConveyorCommands c1c;
    private ISensorProvider c1cS;

    private final SimpleStateVar boxOnC1C = new SimpleStateVar();

    private final SimpleStateVar baseCard1 = new SimpleStateVar();
    private final SimpleStateVar baseCard2 = new SimpleStateVar();

    private final SimpleStateVar cycleDone = new SimpleStateVar();

    private ConveyorBox boxC;
    private ConveyorBox boxD;
    private ConveyorBox base;

    @Override
    public void onInit() {
        c1c = useSkill(IConveyorCommands.class, "C1C");
        c1cS = useSkill(ISensorProvider.class, "C1C");
        c1cS.registerOnSensors(this::onC1C, "s1");

        sh = useSkill(IShuttle.class, "SH1");
        sh.registerOnPosition(2, this::onShAtPos2);

        r1 = useSkill(IRobotCommands.class, "R1");
    }

    @Override
    public void onStart() {
        switchState(100);
        cycleDone.write(false);
    }

    public void state_100() {
        if (boxOnC1C.read() != null && baseCard1.read() != null) {
            boxC = boxOnC1C.readAndForget();
            base = baseCard1.readAndForget();
            switchState(1000);
        } else if (cycleDone.readBoolean()) {
            switchState(200);
        }
    }

    //Spedisce pezzo finito
    public void state_200() {
        schedule.startSerial();
        sh.shuttle();
        setVar(cycleDone, false);      
        schedule.end();
        switchState(210);              
    }

    //Attesa callback 
    public void state_210() {
        if (baseCard1.read() != null) {
            switchState(100); }
    }

    //Start ciclo A
    public void state_1000() {
        onCycleA();
        switchState(1100);
    }

    //Aspetta che il ciclo finisca
    public void state_1100() {
        if (cycleDone.readBoolean()) {
            switchState(100);
        }
    }

    //INPUTS
    private void onC1C(SensorCatch sc) {
        schedule.startSerial();
        c1c.lock(sc.box);
        setVar(boxOnC1C, sc.box);
        schedule.end();
    }

    private void onShAtPos2(SensorCatch t) {
        schedule.startSerial();
        setVar(baseCard1, t.box);
        schedule.end();
    }

    //OPERATIONS
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
}

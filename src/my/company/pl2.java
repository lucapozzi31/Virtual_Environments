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

public class pl2 extends StateMachine {

    private IRobotCommands r2;
    private IShuttle sh;
    private IConveyorCommands c2c;
    private ISensorProvider c2cS;

    private final SimpleStateVar boxOnC2C = new SimpleStateVar();

    private final SimpleStateVar baseCard1 = new SimpleStateVar();
    private final SimpleStateVar baseCard2 = new SimpleStateVar();

    private final SimpleStateVar cycleDone = new SimpleStateVar();

    private ConveyorBox boxC;
    private ConveyorBox boxD;
    private ConveyorBox base;

    @Override
    public void onInit() {
        c2c = useSkill(IConveyorCommands.class, "C2C");
        c2cS = useSkill(ISensorProvider.class, "C2C");
        c2cS.registerOnSensors(this::onC2C, "s1");

        sh = useSkill(IShuttle.class, "SH2");
        sh.registerOnPosition(2, this::onShAtPos2);

        r2 = useSkill(IRobotCommands.class, "R2");
    }

    @Override
    public void onStart() {
        switchState(100);
        cycleDone.write(false);
    }

    public void state_100() {
        if (boxOnC2C.read() != null && baseCard1.read() != null) {
            boxC = boxOnC2C.readAndForget();
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
    private void onC2C(SensorCatch sc) {
        schedule.startSerial();
        c2c.lock(sc.box);
        setVar(boxOnC2C, sc.box);
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
        r2.move(driver.getFrameTransform("Frames.f8_1"), 1000);
        r2.move(driver.getFrameTransform("Frames.f8"), 1000);
        r2.pick(boxC.entity);
        c2c.remove(boxC);
        r2.move(driver.getFrameTransform("Frames.f8_1"), 1000);
        r2.move(driver.getFrameTransform("Frames.f7"), 1000);
        r2.release();
        schedule.attach(boxC.entity, base.entity);
        r2.home();
        setVar(cycleDone, true);
        schedule.end();
    }
}

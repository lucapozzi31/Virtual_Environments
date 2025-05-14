package my.company;

//import com.ttsnetwork.modules.standard.BoxUtils;
import com.ttsnetwork.modules.standard.IConveyorCommands;
import com.ttsnetwork.modules.standard.IRobotCommands;
import com.ttsnetwork.modules.standard.ISensorProvider;
import com.ttsnetwork.modules.standard.IShuttle;
import com.ttsnetwork.modules.standard.ProgrammableLogics;
import com.ttsnetwork.modulespack.conveyors.SensorCatch;
import com.ttsnetwork.modules.standard.SimpleStateVar;
import com.ttsnetwork.modules.standard.StateMachine;
import com.ttsnetwork.modulespack.conveyors.ConveyorBox;

public class pl3 extends StateMachine {

    //Variabili di stato
    //state variables for FSM
    SimpleStateVar sh1Free = new SimpleStateVar();
    SimpleStateVar sh2Free = new SimpleStateVar();
    SimpleStateVar r3Free = new SimpleStateVar();
    SimpleStateVar BoxOnStart = new SimpleStateVar();
    SimpleStateVar Box1ToEject = new SimpleStateVar();
    
    SimpleStateVar PlateOnHand = new SimpleStateVar();

    private IConveyorCommands c1Start, c1Batch;
    private ISensorProvider c1StartS, c1BatchS;
    private IRobotCommands r3;
    private IShuttle sh1;

    private ConveyorBox boxStart;
    private ConveyorBox boxEject;

    @Override
    public void onInit() {
        c1Start = useSkill(IConveyorCommands.class, "C1_StartPick");
        c1StartS = useSkill(ISensorProvider.class, "C1_StartPick");
        c1StartS.registerOnSensors(this::onC1Start, "s1");

        c1Batch = useSkill(IConveyorCommands.class, "C1_Batch");
        c1BatchS = useSkill(ISensorProvider.class, "C1_Batch");
        c1BatchS.registerOnSensors(this::onC1Batch, "s1");

        r3 = useSkill(IRobotCommands.class, "R3");

        sh1 = useSkill(IShuttle.class, "SH1");
        sh1.registerOnPosition(1, this::onSh1Eject);
    }

    @Override
    public void onStart() {
        switchState(100); //stato iniziale
        sh1Free.write(true);
        sh2Free.write(true);
        
    }

    // 100 – Che cosa devo fare?
    public void state_100() {
        if (sh1Free.readBoolean() && BoxOnStart.read() != null) {
            boxStart = BoxOnStart.readAndForget();
            switchState(1000);
        } else if (sh2Free.readBoolean() && BoxOnStart.read() != null) {
            boxStart = BoxOnStart.readAndForget();
            switchState(2000);
        } else if (PlateOnHand.readBoolean() && Box1ToEject.read()!=null) {
            boxEject = Box1ToEject.readAndForget();
            switchState(1200);
        }
    }

    public void state_1000() {
        r3Free.write(false);
        sh1Free.write(false);
        fromStartToSh1();
        switchState(1100);
    }

    public void state_1100() {
        if (r3Free.readBoolean()) {
            switchState(100);
        }
    }

    public void state_1200() {
        r3Free.write(false);
        fromSh1ToBatch();
        switchState(1300);
    }

    public void state_1300() {
        if (r3Free.readBoolean()) {
            switchState(100);
        }
    }

    //OPERATIONS
    private void fromStartToSh1() {
        schedule.startSerial();
        r3.move(driver.getFrameTransform("Frames.f1"), 2000);
        //r3.move(boxStart., 1000);
        r3.pick(boxStart.entity);
        r3.move(driver.getFrameTransform("Frames.f1_1"), 2000);
        c1Start.remove(boxStart);
        r3.move(driver.getFrameTransform("Frames.f2"), 2000);
        r3.release();
        r3.home();
        sh1.insert(1, boxStart);
        sh1.shuttle();
        setVar(r3Free, true);
        schedule.end();
    }

    private void fromSh1ToBatch() {
        schedule.startSerial();
        r3.move(driver.getFrameTransform("Frames.f2"), 2000);
        r3.pick(boxStart.entity);
        sh1.remove(1);
        r3.move(driver.getFrameTransform("Frames.f5_1"), 1000);
        r3.move(driver.getFrameTransform("Frames.f5"), 1000);
        r3.release();
        r3.home();
        c1Batch.release(boxStart);
        setVar(r3Free, true);
        setVar(sh1Free, true);
        setVar(Box1ToEject, false);
        schedule.end();
    }

    //TUTTI I VARI INPUT
    //Pezzo arrivato su C1Start
    private void onC1Start(SensorCatch bx) {
        schedule.startSerial();
        c1Start.lock(bx.box);
        setVar(BoxOnStart, bx.box);
        schedule.end();
    }
    
    private void onSh1Eject(SensorCatch bx){
        setVar(Box1ToEject, bx.box);
    }

    //Sensore C1 Batch
    private void onC1Batch(SensorCatch bx) {
        schedule.startSerial();
        c1Batch.lock(bx.box);
        setVar(PlateOnHand, true);
        schedule.end();
    }
    
}

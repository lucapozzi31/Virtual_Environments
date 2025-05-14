package my.company;

//import com.ttsnetwork.modules.standard.BoxUtils;
import com.ttsnetwork.modules.standard.IConveyorCommands;
import com.ttsnetwork.modules.standard.IRobotCommands;
import com.ttsnetwork.modules.standard.ISensorProvider;
import com.ttsnetwork.modules.standard.IShuttle;
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
    SimpleStateVar Box2ToEject = new SimpleStateVar();

    SimpleStateVar PlateOnHand = new SimpleStateVar(); //Boolean c'è il vassoio?
    SimpleStateVar PlateOnLoad = new SimpleStateVar();

    private IConveyorCommands c1Start, c1Batch;
    private ISensorProvider c1StartS, c1BatchS;
    private IRobotCommands r3;
    private IShuttle sh1, sh2;

    private ConveyorBox boxStart;
    private ConveyorBox boxEject;
    private ConveyorBox plateEject;

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
        sh2 = useSkill(IShuttle.class, "SH2");
        sh2.registerOnPosition(1, this::onSh2Eject);
    }

    @Override
    public void onStart() {
        switchState(100); //stato iniziale
        sh1Free.write(true);
        sh2Free.write(true);
        PlateOnHand.write(false);

    }

    // 100 – Che cosa devo fare?
    public void state_100() {
        if (sh1Free.readBoolean() && BoxOnStart.read() != null) {
            boxStart = BoxOnStart.readAndForget();
            switchState(1000);
        } else if (sh2Free.readBoolean() && BoxOnStart.read() != null) {
            boxStart = BoxOnStart.readAndForget();
            switchState(2000);
        } else if (PlateOnHand.readBoolean() && Box1ToEject.read() != null) {
            boxEject = Box1ToEject.readAndForget();
            switchState(1200);
        } else if (PlateOnHand.readBoolean() && Box2ToEject.read() != null) {
            boxEject = Box2ToEject.readAndForget();
            switchState(2200);
        }
    }

//Carica il pezzo da start a SH1
    public void state_1000() {
        r3Free.write(false);
        sh1Free.write(false);
        fromStartToSh1();
        switchState(1100);
    }

//Aspetta che il robot sia libero
    public void state_1100() {
        if (r3Free.readBoolean()) {
            switchState(100);
        }
    }

    //Scarica il pezzo da SH1 al Vassoio
    public void state_1200() {
        r3Free.write(false);
        fromSh1ToBatch();
        switchState(1300);
    }

    //Aspetta che il robot sia libero
    public void state_1300() {
        if (r3Free.readBoolean()) {
            switchState(100);
        }
    }

    //Carica il pezzo da start a sh2
    public void state_2000() {
        r3Free.write(false);
        sh2Free.write(false);
        fromStartToSh2();
        switchState(2100);
    }

    //Aspetta che il robot sia libero
    public void state_2100() {
        if (r3Free.readBoolean()) {
            switchState(100);
        }
    }

    //Scarica il pezzo da SH1 al Vassoio
    public void state_2200() {
        r3Free.write(false);
        fromSh2ToBatch();
        switchState(2300);
    }

    //Aspetta che il robot sia libero
    public void state_2300() {
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

    private void fromStartToSh2() {
        schedule.startSerial();
        r3.move(driver.getFrameTransform("Frames.f1"), 2000);
        //r3.move(boxStart., 1000);
        r3.pick(boxStart.entity);
        r3.move(driver.getFrameTransform("Frames.f1_1"), 2000);
        c1Start.remove(boxStart);
        r3.move(driver.getFrameTransform("Frames.f6"), 2000);
        r3.release();
        r3.home();
        sh2.insert(1, boxStart);
        sh2.shuttle();
        setVar(r3Free, true);
        schedule.end();
    }


    private void fromSh1ToBatch() {
        schedule.startSerial();

        // leggi entrambi prima
        plateEject = (ConveyorBox) PlateOnLoad.readAndForget();
        ConveyorBox part = boxEject;

        r3.move(driver.getFrameTransform("Frames.f2"), 2000);
        r3.pick(part.entity);
        sh1.remove(1);
        r3.move(driver.getFrameTransform("Frames.f1_1"), 1000);
        r3.move(driver.getFrameTransform("Frames.f5"), 1000);
        r3.release();

        // attacca il pezzo sopra al vassoio
        schedule.attach(part.entity, plateEject.entity);

        r3.home();
        c1Batch.release(plateEject);
        setVar(PlateOnHand, false);
        setVar(r3Free, true);
        setVar(sh1Free, true);

        schedule.end();
    }


private void fromSh2ToBatch() {
        schedule.startSerial();
        r3.move(driver.getFrameTransform("Frames.f6"), 2000);
        r3.pick(boxEject.entity);
        sh1.remove(1);
        r3.move(driver.getFrameTransform("Frames.f1_1"), 1000);
        r3.move(driver.getFrameTransform("Frames.f5"), 1000);
        r3.release();
        r3.home();
        c1Batch.release(boxEject);
        setVar(PlateOnHand, false);
        setVar(r3Free, true);
        setVar(sh1Free, true);
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

    private void onSh1Eject(SensorCatch t) {
        schedule.startSerial();
        setVar(Box1ToEject, t.box);
        schedule.end();
    }

    private void onSh2Eject(SensorCatch t) {
        schedule.startSerial();
        setVar(Box2ToEject, t.box);
        schedule.end();
    }

    //Sensore C1 Batch
    private void onC1Batch(SensorCatch bx) {
        schedule.startSerial();
        c1Batch.lock(bx.box);
        setVar(PlateOnLoad, bx.box);
        setVar(PlateOnHand, true);
        schedule.end();
    }

}

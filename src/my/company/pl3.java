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
import com.ttsnetwork.modules.standard.*;

public class pl3 extends StateMachine {

    //Variabili di stato
    //state variables for FSM
    SimpleStateVar sh1Free = new SimpleStateVar();
    SimpleStateVar sh2Free = new SimpleStateVar();
    SimpleStateVar r3Free = new SimpleStateVar();
    SimpleStateVar BoxOnStart = new SimpleStateVar();
    SimpleStateVar Box1ToEject = new SimpleStateVar();
    SimpleStateVar Box2ToEject = new SimpleStateVar();

    SimpleStateVar Rfid_Shuttle1 = new SimpleStateVar();
    SimpleStateVar Rfid_Shuttle2 = new SimpleStateVar();

    SimpleStateVar BatchType = new SimpleStateVar(); //Che tipo sto caricando?
    SimpleStateVar PlateOnLoad = new SimpleStateVar(); //Entity vassoio
    SimpleStateVar BatchFull = new SimpleStateVar(); //Boolean

    SimpleStateVar BatchCount = new SimpleStateVar();   // Integer

    private IConveyorCommands c1Start, c1Batch;
    private ISensorProvider c1StartS, c1BatchS;
    private IRobotCommands r3;
    private IShuttle sh1, sh2;

    private ConveyorBox boxStart;
    private ConveyorBox boxEject;
    private ConveyorBox boxEject2;
    private ConveyorBox plateEject;

    private static final int CapacityA1 = 10;
    private static final int CapacityA2 = 9;

    //Coordinate offset pezzi
    private double[] xA2 = new double[]{-160, 0, 160, -160, 0, 160, -160, 0, 160};
    private double[] yA2 = new double[]{105, 105, 105, 0, 0, 0, -105, -105, -105};
    private double[] xA1 = new double[]{-210, -105, 0, 105, 210, -210, -105, 0, 105, 210};
    private double[] yA1 = new double[]{72.5,72.5,72.5,72.5,72.5,-72.5,-72.5,-72.5,-72.5,-72.5};

    private int BatchPlateCount;

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
        BatchCount.write(0);
        BatchFull.write(false);

    }

    // 100 – Che cosa devo fare?
    public void state_100() {

        if (PlateOnLoad.read() != null
                && Box1ToEject.read() != null
                && Box2ToEject.read() != null
                && BatchType.read() != null
                && !BatchType.read().equals(Rfid_Shuttle1.read())
                && !BatchType.read().equals(Rfid_Shuttle2.read())) {

            switchState(6000);

        } else if (PlateOnLoad.read() != null
                && Box1ToEject.read() != null
                && (BatchType.read() == null
                || BatchType.read().equals(Rfid_Shuttle1.read()))) {

            boxEject = Box1ToEject.readAndForget();
            switchState(3000);

        } else if (PlateOnLoad.read() != null
                && Box2ToEject.read() != null
                && (BatchType.read() == null
                || BatchType.read().equals(Rfid_Shuttle2.read()))) {

            boxEject2 = Box2ToEject.readAndForget();
            switchState(4000);

        } else if (sh1Free.readBoolean() && BoxOnStart.read() != null) {
            boxStart = BoxOnStart.readAndForget();
            switchState(1000);

        } else if (sh2Free.readBoolean() && BoxOnStart.read() != null) {
            boxStart = BoxOnStart.readAndForget();
            switchState(2000);

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
    public void state_3000() {
        r3Free.write(false);
        fromSh1ToBatch();
        switchState(3100);
    }

    //Aspetta che il robot sia libero
    public void state_3100() {
        if (r3Free.readBoolean()) {
            switchState(3200);
        }
    }

    //Il vassoio è pieno?
    public void state_3200() {
        if (BatchFull.readBoolean()) {
            switchState(6000);
        } else {
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

    //Scarica il pezzo da SH2 al Vassoio
    public void state_4000() {
        r3Free.write(false);
        fromSh2ToBatch();
        switchState(4100);
    }

    //Aspetta che il robot sia libero
    public void state_4100() {
        if (r3Free.readBoolean()) {
            switchState(4200);
        }
    }

    //Il vassoio è pieno?
    public void state_4200() {
        if (BatchFull.readBoolean()) {
            switchState(6000);
        } else {
            switchState(100);
        }
    }

    //Release del conveyor con i vassoi
    public void state_6000() {
        releaseBatch();
        switchState(100);
    }

    //OPERATIONS
    private void fromStartToSh1() {

        schedule.startSerial();
        r3.move(driver.getFrameTransform("Frames.f1"), 2000);
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

        plateEject = (ConveyorBox) PlateOnLoad.read();
        ConveyorBox part = boxEject;
        String type = part.entity.getProperty(String.class, "rfid");
        setVar(this.BatchType, type);
        Integer n = BatchCount.read();

        //Variabili offset
        double x;
        double y;
        int r;
        if ("P001".equals(type)) {
            x = xA1[BatchPlateCount];
            y = yA1[BatchPlateCount];
            r=90;
        } else {
            x = xA2[BatchPlateCount];
            y = yA2[BatchPlateCount];
            r=0;
        }
        
        schedule.startSerial();
        r3.move(driver.getFrameTransform("Frames.f2"), 2000);
        r3.pick(part.entity);
        sh1.remove(1);
        r3.move(driver.getFrameTransform("Frames.f1_1"), 1000);
        r3.move(BoxUtils.targetOffset(plateEject, x, y, BoxUtils.zSize(plateEject) + 100, 0, 0, r), 1000);
        r3.release();
        schedule.attach(part.entity, plateEject.entity);

        BatchPlateCount++;
        
        if ("P001".equals(Rfid_Shuttle1.read()) && BatchPlateCount >= CapacityA1) {
            setVar(BatchFull, true);
        } else if ("P002".equals(Rfid_Shuttle1.read()) && BatchPlateCount >= CapacityA2) {
            setVar(BatchFull, true);
        }
        
        r3.home();
        setVar(sh1Free, true);
        setVar(r3Free, true);
        schedule.callFunction(this::update);
        schedule.end();
    }

    private void fromSh2ToBatch() {
        plateEject = (ConveyorBox) PlateOnLoad.read();
        ConveyorBox part = boxEject2;
        String type = part.entity.getProperty(String.class, "rfid");
        setVar(this.BatchType, type);
        
         //Variabili offset
        double x;
        double y;
        int r;
        if ("P001".equals(type)) {
            x = xA1[BatchPlateCount];
            y = yA1[BatchPlateCount];
            r=90;
        } else {
            x = xA2[BatchPlateCount];
            y = yA2[BatchPlateCount];
            r=0;
        }
        
        schedule.startSerial();
        r3.move(driver.getFrameTransform("Frames.f6"), 2000);
        r3.pick(part.entity);
        sh2.remove(1);
        r3.move(driver.getFrameTransform("Frames.f1_1"), 1000);
        r3.move(BoxUtils.targetOffset(plateEject, x, y, BoxUtils.zSize(plateEject) + 100, 0, 0, r), 1000);
        r3.release();
        schedule.attach(part.entity, plateEject.entity);

        BatchPlateCount++;
        
        if ("P001".equals(Rfid_Shuttle1.read()) && BatchPlateCount >= CapacityA1) {
            setVar(BatchFull, true);
        } else if ("P002".equals(Rfid_Shuttle1.read()) && BatchPlateCount >= CapacityA2) {
            setVar(BatchFull, true);
        }

        r3.home();
        setVar(r3Free, true);
        setVar(sh2Free, true);
        schedule.callFunction(this::update);

        schedule.end();
    }

    private void releaseBatch() {
        setVar(PlateOnLoad, null);
        setVar(BatchType, null);

        //contatore + flag
        setVar(BatchCount, 0);
        setVar(BatchFull, false);
        BatchPlateCount = 0;

        schedule.startSerial();
        c1Batch.release(plateEject);
        schedule.end();
    }

    //TUTTI GLI INPUT
    private void onC1Start(SensorCatch bx) {
        schedule.startSerial();
        c1Start.lock(bx.box);
        setVar(BoxOnStart, bx.box);
        schedule.end();
    }

    private void onSh1Eject(SensorCatch t) {
        String id = t.box.entity.getProperty(String.class, "rfid");
        setVar(Rfid_Shuttle1, id);
        schedule.startSerial();
        setVar(Box1ToEject, t.box);
        schedule.end();
    }

    private void onSh2Eject(SensorCatch t) {
        String id = t.box.entity.getProperty(String.class, "rfid");
        setVar(Rfid_Shuttle2, id);
        schedule.startSerial();
        setVar(Box2ToEject, t.box);
        schedule.end();
    }

    private void onC1Batch(SensorCatch bx) {

        setVar(BatchType, null);
        setVar(PlateOnLoad, bx.box);     //  ora è visibile al prossimo state_100

        schedule.startSerial();
        c1Batch.lock(bx.box);
        schedule.end();
    }

}

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
    private final SimpleStateVar boxOnC1D = new SimpleStateVar();
    private final SimpleStateVar baseCard1 = new SimpleStateVar();
    private final SimpleStateVar baseCard2 = new SimpleStateVar();
    private final SimpleStateVar cycleDone = new SimpleStateVar();
    private final SimpleStateVar cycleDoneLong = new SimpleStateVar();
    private final SimpleStateVar rfidVar = new SimpleStateVar();
    private int CountC;
    private int CountD;
    private int Time;

    private ConveyorBox boxC;
    private ConveyorBox boxD;
    private ConveyorBox base;

    private String curRfid;

    private double[] xA1_C = new double[]{-50, -30};
    private double[] xA1_D = new double[]{-50, 50, 50, 50};
    private double[] xA2_C = new double[]{-50, 0, 50, 0};
    private double[] xA2_D = new double[]{-20, -20, -50, 50};

    private double[] yA1_C = new double[]{-20, 20};
    private double[] yA1_D = new double[]{20, 20, 0, -20};
    private double[] yA2_C = new double[]{-30, -20, -20, 20};
    private double[] yA2_D = new double[]{-30, -10, 20, 20};

    @Override
    public void onInit() {
        c1c = useSkill(IConveyorCommands.class, "C1C");
        c1cS = useSkill(ISensorProvider.class, "C1C");
        c1cS.registerOnSensors(this::onC1C, "s1");

        c1d = useSkill(IConveyorCommands.class, "C1D");
        c1dS = useSkill(ISensorProvider.class, "C1D");
        c1dS.registerOnSensors(this::onC1D, "s1");

        sh = useSkill(IShuttle.class, "SH1");
        sh.registerOnPosition(2, this::onShAtPos2);

        r1 = useSkill(IRobotCommands.class, "R1");
        Time=1000;
    }

    @Override
    public void onStart() {
        CountC = 0;
        CountD = 0;
        cycleDone.write(false);
        switchState(100);
    }
    // stato cosa devo fare?

    public void state_100() {
        if (baseCard1.read() != null && rfidVar.read() != null) {
            base = baseCard1.readAndForget();
            curRfid = (String) rfidVar.readAndForget();
            switchState(1000);
        }
    }

    public void state_1000() {
        CountC = 0;
        if ("P001".equals(curRfid)) {
            switchState(2000);
        } else if ("P002".equals(curRfid)) {
            switchState(4000);
        }

    }

//Pezzo P001
    public void state_2000() {
        if (CountC < 2 && boxOnC1C.read() != null) {
            boxC = boxOnC1C.readAndForget();
            CountC++;
            switchState(2100);
        } else if (CountD < 4 && boxOnC1D.read() != null) {
            boxD = boxOnC1D.readAndForget();
            CountD++;
            switchState(3100);
        } else if (CountC == 2 && CountD == 4) {
            switchState(200);
        }
    }

    //Mettere il pezzo C sulla scheda tipo P001
    public void state_2100() {
        cycleDone.write(false);//-------------------------
        onCycleC();
        switchState(2200);
    }

    public void state_2200() {
        if (cycleDone.readBoolean()) {
            boxC = null;
            switchState(2000);
        }
    }

    public void state_3100() {
        cycleDone.write(false);//----------------------
        onCycleD();
        switchState(3200);
    }

    public void state_3200() {
        if (cycleDone.readBoolean()) {
            boxD = null;
            switchState(2000);
        }
    }

    //boxC != null
    //Pezzo P002
    public void state_4000() {
        if (CountC < 4 && boxOnC1C.read() != null) {
            boxC = boxOnC1C.readAndForget();
            CountC++;
            switchState(4100);
        } else if (CountD < 4 && boxOnC1D.read() != null) {
            boxD = boxOnC1D.readAndForget();
            CountD++;
            switchState(5100);
        } else if (CountC == 4 && CountD == 4) {
            switchState(200);
        }
    }

    //Mettere il pezzo C sulla scheda tipo P002
    public void state_4100() {
        cycleDone.write(false);//----------------------
        onCycleC();
        switchState(4200);
    }

    public void state_4200() {
        if (cycleDone.readBoolean()) {
            boxC = null;
            switchState(4000);
        }
    }

    public void state_5100() {
        cycleDone.write(false);//----------------------
        onCycleD();
        switchState(5200);
    }

    public void state_5200() {
        if (cycleDone.readBoolean()) {
            boxD = null;
            switchState(4000);
        }
    }

    //Spedisce pezzo finito
    public void state_200() {
        CountC = 0;
        CountD = 0;
        schedule.startSerial();
        sh.shuttle();
        schedule.end();
        switchState(100);
    }

    //Attesa callback 
    public void state_210() {
        if (baseCard1.read() != null) {
            switchState(100);
        }
    }

    private void onC1C(SensorCatch sc) {
        schedule.startSerial();
        c1c.lock(sc.box);
        setVar(boxOnC1C, sc.box);
        schedule.end();

    }

    private void onC1D(SensorCatch sc) {
        schedule.startSerial();
        c1d.lock(sc.box);
        //boxD = sc.box;
        setVar(boxOnC1D, sc.box);
        schedule.end();

    }

    private void onShAtPos2(SensorCatch t) {
        String rfid = t.box.entity.getProperty(String.class,
                "rfid");
        setVar(rfidVar, rfid);
        schedule.startSerial();
        setVar(baseCard1, t.box);
        schedule.end();
    }

    private void onCycleC() {
        double x;
        double y;
        if ("P001".equals(curRfid)) {
            x = xA1_C[CountC - 1];
            y = yA1_C[CountC - 1];
        } else {
            x = xA2_C[CountC - 1];
            y = yA2_C[CountC - 1];
        }
        schedule.startSerial();
        {
            r1.move(driver.getFrameTransform("Frames.f3_1"), Time);
            r1.move(driver.getFrameTransform("Frames.f4"), Time);
            r1.moveLinear(BoxUtils.targetTop(boxC), Time);
            r1.pick(boxC.entity);
            c1c.remove(boxC);
            r1.move(driver.getFrameTransform("Frames.f3_1"), Time);
//        r1.move(driver.getFrameTransform("Frames.f3"), 1000);
            r1.move(BoxUtils.targetOffset(base, x, y, BoxUtils.zSize(base)+10, 0, 0, 0), 1000);
            r1.release();
            schedule.attach(boxC.entity, base.entity);
            r1.home();
            setVar(cycleDone, true);
            schedule.end();
        }
    }

    private void onCycleD() {
        double x;
        double y;
        if ("P001".equals(curRfid)) {
            x = xA1_D[CountD - 1];
            y = yA1_D[CountD - 1];
        } else {
            x = xA2_D[CountD - 1];
            y = yA2_D[CountD - 1];
        }
        schedule.startSerial();
        {
            r1.move(driver.getFrameTransform("Frames.f3_1"), Time);
            r1.move(driver.getFrameTransform("Frames.f4_1"), Time);
            r1.moveLinear(BoxUtils.targetTop(boxD), Time);
            r1.pick(boxD.entity);
            c1d.remove(boxD);
            r1.move(driver.getFrameTransform("Frames.f3_1"), Time);
//        r1.move(driver.getFrameTransform("Frames.f3"), 1000);
            r1.move(BoxUtils.targetOffset(base, x, y, BoxUtils.zSize(base)+10, 0, 0, 0), 1000);
            r1.release();
            schedule.attach(boxD.entity, base.entity);
            r1.home();
            setVar(cycleDone, true);
            schedule.end();
        }
    }
}

package my.company;

import com.ttsnetwork.modules.standard.BoxUtils;
import com.ttsnetwork.modules.standard.IConveyorCommands;
import com.ttsnetwork.modules.standard.IRobotCommands;
import com.ttsnetwork.modules.standard.ISensorProvider;
import com.ttsnetwork.modules.standard.IShuttle;
import com.ttsnetwork.modules.standard.ProgrammableLogics;
import com.ttsnetwork.modulespack.conveyors.SensorCatch;

public class pl3 extends ProgrammableLogics {

    public LineInfo sharedState;
    
    private IConveyorCommands c1Start;
    private ISensorProvider c1StartS;
    private ISensorProvider c1cS;

    private IConveyorCommands c1Batch;
    private ISensorProvider c1BatchS;

    private IRobotCommands r3;
    private IShuttle shuttle1;

    private SensorCatch trayOnBatch;

    @Override
    public void onInit() {
        c1Start = useSkill(IConveyorCommands.class, "C1_StartPick");
        c1StartS = useSkill(ISensorProvider.class, "C1_StartPick");
        c1StartS.registerOnSensors(this::onC1Start, "s1");

        c1cS = useSkill(ISensorProvider.class, "C1C");
        c1cS.registerOnSensors(this::onC1C, "s1");

        c1Batch = useSkill(IConveyorCommands.class, "C1_Batch");
        c1BatchS = useSkill(ISensorProvider.class, "C1_Batch");
        c1BatchS.registerOnSensors(this::onC1Batch, "s1");

        r3 = useSkill(IRobotCommands.class, "R3");

        shuttle1 = useSkill(IShuttle.class, "SH1");
        shuttle1.registerOnPosition(1, this::onPos1);
        shuttle1.registerOnPosition(2, this::onPos2);
    }

    @Override
    public void onStart() {
    }

    private void onC1Start(SensorCatch sc) {
        schedule.startSerial();
        c1Start.lock(sc.box);
        r3.move(driver.getFrameTransform("Frames.f1"), 2000);
        r3.move(BoxUtils.targetOffset(sc.box, 0, 0, 100, 0, 0, 0), 1000);
        r3.pick(sc.box.entity);
        r3.move(driver.getFrameTransform("Frames.f1_1"), 2000);
        r3.move(driver.getFrameTransform("Frames.f2"), 2000);
        r3.release();
        r3.home();
        shuttle1.insert(1, sc.box);
        shuttle1.shuttle();
        schedule.end();
    }

    private void onC1Batch(SensorCatch sc) {
        schedule.startSerial();
        c1Batch.lock(sc.box);
        trayOnBatch = sc;
        schedule.end();
    }

    private void onC1C(SensorCatch sc) {
    }

    private void onPos1(SensorCatch sc) {
        if (trayOnBatch == null || !sharedState.assemblaggioCompletatoStazione1 || sharedState.pezziSuShuttle1 == 0) {
            return;
        }

        schedule.startSerial();

        r3.move(driver.getFrameTransform("Frames.f3"), 2000);
        r3.pick(sc.box.entity);
        r3.move(driver.getFrameTransform("Frames.f5_1"), 1000);
        r3.move(BoxUtils.targetOffset(trayOnBatch.box, 0, 0, 100 + BoxUtils.zSize(trayOnBatch.box), 0, 0, 0), 1500);
        r3.release();
        r3.home();

        schedule.attach(sc.box.entity, trayOnBatch.box.entity);
        c1Batch.release(trayOnBatch.box);
        trayOnBatch = null;

        sharedState.assemblaggioCompletatoStazione1 = false;
        sharedState.pezziSuShuttle1 = 0;

        schedule.end();
    }

    private void onPos2(SensorCatch sc) {
    }
}

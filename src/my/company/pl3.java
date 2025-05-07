package my.company;

import com.ttsnetwork.modules.standard.BoxUtils;
import com.ttsnetwork.modules.standard.IConveyorCommands;
import com.ttsnetwork.modules.standard.IRobotCommands;
import com.ttsnetwork.modules.standard.ISensorProvider;
import com.ttsnetwork.modules.standard.IShuttle;
import com.ttsnetwork.modules.standard.ProgrammableLogics;
import com.ttsnetwork.modules.standard.SimpleStateVar;
import com.ttsnetwork.modulespack.conveyors.SensorCatch;

public class pl3 extends ProgrammableLogics {

    public LineInfo sharedState;

    private IConveyorCommands c1Start, c1Batch;
    private ISensorProvider c1StartS, c1BatchS;
    private IRobotCommands r3;
    private IShuttle sh1;

    private final SimpleStateVar pendingPiece = new SimpleStateVar();  // SensorCatch
    private final SimpleStateVar batchTray = new SimpleStateVar();  // SensorCatch

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
        sh1.registerOnPosition(1, this::onPos1);
    }

    /* ---------- SENSOR C1_StartPick ---------- */
    private void onC1Start(SensorCatch sc) {
        schedule.startSerial();
        c1Start.lock(sc.box);              // blocco sempre il pezzo
        pendingPiece.write(sc);            // lo metto in coda
        schedule.callFunction(this::tryLoadShuttle); // tentativo dopo chiusura serial
        schedule.end();
    }

    /* ---------- SENSOR C1_Batch ---------- */
    private void onC1Batch(SensorCatch sc) {
        schedule.startSerial();
        c1Batch.lock(sc.box);
        batchTray.write(sc);
        schedule.end();
    }

    /* ---------- PROVA A CARICARE LO SHUTTLE ---------- */
    private void tryLoadShuttle() {
        SensorCatch piece = (SensorCatch) pendingPiece.read();
        if (piece == null) {
            return;
        }
        if (sharedState.shuttle1Busy || !sharedState.assemblyDoneStation1) {
            pendingPiece.write(piece);     // riproveremo più tardi
            return;
        }

        sharedState.shuttle1Busy = true;
        sharedState.assemblyDoneStation1 = false;

        schedule.startSerial();

        r3.move(driver.getFrameTransform("Frames.f1"), 2000);
        r3.move(BoxUtils.targetOffset(piece.box, 0, 0, 100, 0, 0, 0), 1000);
        r3.pick(piece.box.entity);
        r3.move(driver.getFrameTransform("Frames.f1_1"), 2000);
        r3.move(driver.getFrameTransform("Frames.f2"), 2000);
        r3.release();
        r3.home();
        c1Start.release(piece.box);

        sh1.insert(1, piece.box);
        sh1.shuttle();

        schedule.end();
    }

    /* ---------- SHUTTLE POS-1: SCARICO ---------- */
    private void onPos1(SensorCatch sc) {
        SensorCatch tray = (SensorCatch) batchTray.read();
        if (tray == null) {
            return;
        }

        schedule.startSerial();

        r3.move(driver.getFrameTransform("Frames.f3"), 2000);
        r3.pick(sc.box.entity);
        r3.move(driver.getFrameTransform("Frames.f5_1"), 1000);
        r3.move(driver.getFrameTransform("Frames.f5"), 1000);
        r3.release();
        r3.home();

        schedule.attach(sc.box.entity, tray.box.entity);
        c1Batch.release(tray.box);
        batchTray.write(null);

        sharedState.shuttle1Busy = false;
        sharedState.assemblyDoneStation1 = true;

        schedule.callFunction(this::tryLoadShuttle); // subito pronto per il prossimo
        schedule.end();
    }
}

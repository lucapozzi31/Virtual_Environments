package my.company;

import com.ttsnetwork.modules.standard.BoxUtils;
import com.ttsnetwork.modules.standard.IConveyorCommands;
import com.ttsnetwork.modules.standard.IRobotCommands;
import com.ttsnetwork.modules.standard.ISensorProvider;
import com.ttsnetwork.modules.standard.IShuttle;
import com.ttsnetwork.modules.standard.ProgrammableLogics;
import com.ttsnetwork.modulespack.conveyors.ConveyorBox;
import com.ttsnetwork.modulespack.conveyors.SensorCatch;

public class pl1 extends ProgrammableLogics {

    public LineInfo sharedState;

    private IRobotCommands robot;
    private IShuttle shuttle;

    private IConveyorCommands c1c;
    private ISensorProvider c1cSensors;

    private ConveyorBox boxC1C;

    @Override
    public void onInit() {
        c1c = useSkill(IConveyorCommands.class, "C1C");
        c1cSensors = useSkill(ISensorProvider.class, "C1C");
        c1cSensors.registerOnSensors(this::onSensorC1C, "s1");

        shuttle = useSkill(IShuttle.class, "SH1");
        shuttle.registerOnPosition(2, this::onShuttleAtPos2);

        robot = useSkill(IRobotCommands.class, "R1");
    }

    @Override
    public void onStart() {
    }

    private void onSensorC1C(SensorCatch sc) {
        schedule.startSerial();
        c1c.lock(sc.box);
        boxC1C = sc.box;
        schedule.end();
    }

    private void onShuttleAtPos2(SensorCatch sc) {
        ConveyorBox boxOnShuttle = sc.box;
        if (boxC1C == null || boxOnShuttle == null) {
            return;
        }

        schedule.startSerial();

        robot.move(driver.getFrameTransform("Frames.f4"), 1000);
        robot.pick(boxC1C.entity);
        c1c.remove(boxC1C); // importante: rimuoviamo la box dal nastro

        robot.move(
                BoxUtils.targetOffset(
                        boxOnShuttle,
                        0, 0,
                        BoxUtils.zSize(boxOnShuttle) + BoxUtils.zSize(boxC1C),
                        0, 0, 0),
                1000);

        schedule.attach(boxC1C.entity, boxOnShuttle.entity);

        robot.release();
        robot.home();

        boxC1C = null;

        if (sharedState != null) {
            sharedState.assemblaggioCompletatoStazione1 = true;
            sharedState.pezziSuShuttle1 = 1;

        }

        shuttle.shuttle();
        schedule.end();
    }
}

// Pl1.java
package my.company;

import com.ttsnetwork.modules.standard.BoxUtils;
import com.ttsnetwork.modules.standard.IConveyorCommands;
import com.ttsnetwork.modules.standard.IRobotCommands;
import com.ttsnetwork.modules.standard.ISensorProvider;
import com.ttsnetwork.modules.standard.IShuttle;
import com.ttsnetwork.modules.standard.ProgrammableLogics;
import com.ttsnetwork.modules.standard.SimpleStateVar;
import com.ttsnetwork.modulespack.conveyors.ConveyorBox;
import com.ttsnetwork.modulespack.conveyors.SensorCatch;

public class pl1 extends ProgrammableLogics {

    public LineInfo sharedState;

    private IRobotCommands r1;
    private IShuttle      sh1;
    private IConveyorCommands c1c;
    private ISensorProvider   c1cS;

    private final SimpleStateVar boxOnC1C = new SimpleStateVar();

    @Override
    public void onInit() {
        c1c  = useSkill(IConveyorCommands.class, "C1C");
        c1cS = useSkill(ISensorProvider.class,   "C1C");
        c1cS.registerOnSensors(this::onC1C, "s1");

        sh1 = useSkill(IShuttle.class, "SH1");
        sh1.registerOnPosition(2, this::onShAtPos2);

        r1 = useSkill(IRobotCommands.class, "R1");
    }

    private void onC1C(SensorCatch sc) {
        schedule.startSerial();
        c1c.lock(sc.box);
        boxOnC1C.write(sc.box);        // memorizzo il box per lo shuttle
        schedule.end();
    }

    private void onShAtPos2(SensorCatch sc) {
        ConveyorBox convBox = (ConveyorBox) boxOnC1C.readAndForget();
        ConveyorBox stackBox = sc.box;
        if (convBox == null || stackBox == null) return;

        schedule.startSerial();

        r1.move(driver.getFrameTransform("Frames.f3_1"), 1000);
        r1.move(driver.getFrameTransform("Frames.f4"),     1000);
        r1.pick(convBox.entity);
        c1c.remove(convBox);

        r1.move(driver.getFrameTransform("Frames.f3_1"), 1000);
        r1.move(driver.getFrameTransform("Frames.f3"), 1000);
        /*r1.move(BoxUtils.targetOffset(
                   stackBox,
                   0, 0,
                   BoxUtils.zSize(stackBox) + BoxUtils.zSize(convBox),
                   0, 0, 0), 1000);*/
        
        r1.release();
        schedule.attach(convBox.entity, stackBox.entity);

        r1.home();

        schedule.callFunction(() -> sharedState.assemblyDoneStation1 = true);
        sh1.shuttle();                 // ritorno a Pos-1
        schedule.end();
    }
}

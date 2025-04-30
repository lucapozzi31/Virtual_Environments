/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package my.company;

/**
 *
 * @author ciavr
 */
import com.ttsnetwork.modules.standard.BoxUtils;
import com.ttsnetwork.modules.standard.IConveyorCommands;
import com.ttsnetwork.modules.standard.IRobotCommands;
import com.ttsnetwork.modules.standard.ISensorProvider;
import com.ttsnetwork.modules.standard.ISource;
import com.ttsnetwork.modules.standard.ProgrammableLogics;
import com.ttsnetwork.modulespack.conveyors.ConveyorBox;
import com.ttsnetwork.modulespack.conveyors.SensorCatch;

//Programmazione del robot nella prima postazione di assemblaggio
public class pl3 extends ProgrammableLogics {

   // IConveyorCommands c1commands;
    IConveyorCommands ciniz1commands;
    IConveyorCommands c2commands;
    ISensorProvider ciniz1sensors;
    IRobotCommands r3commands;
    IConveyorCommands sh1commands;

    @Override
    public void onInit() {

        ciniz1commands = useSkill(IConveyorCommands.class, "ciniz1");
        ciniz1sensors = useSkill(ISensorProvider.class, "ciniz1");
        ciniz1sensors.registerOnSensors(this::onSensori);
        r3commands = useSkill(IRobotCommands.class, "r3");
        sh1commands = useSkill(IConveyorCommands.class, "sh1");

    }

    private void onSensori(SensorCatch t) {

        schedule.startSerial();
        {
            ciniz1commands.lock(t.box);
            r3commands.move(driver.getFrameTransform("Path.f1"), 2000);
            r3commands.move(BoxUtils.targetOffset(t.box, 0, 0, 100, 0, 0, 0), 1000);
            r3commands.pick(t.box.entity);
            ciniz1commands.remove(t.box);
            r3commands.move(driver.getFrameTransform("Parh2.f2"), 5000);
            r3commands.move(driver.getFrameTransform("C2.startFrame"), t.box.cF, 1500);
            r3commands.release();
            sh1commands.insert(t.box);
            r3commands.home();
        }

        schedule.end();

    }

}

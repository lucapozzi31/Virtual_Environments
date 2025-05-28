/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package my.company;

import com.ttsnetwork.modules.standard.BoxUtils;
import com.ttsnetwork.modules.standard.IConveyorCommands;
import com.ttsnetwork.modules.standard.IRobotCommands;
import com.ttsnetwork.modules.standard.ISensorProvider;
import com.ttsnetwork.modules.standard.ISource;
import com.ttsnetwork.modules.standard.ProgrammableLogics;
import com.ttsnetwork.modulespack.conveyors.ConveyorBox;
import com.ttsnetwork.modulespack.conveyors.SensorCatch;
import java.util.List;

/**
 *
 * @author ciavr
 */
public class pl6 extends ProgrammableLogics {

    IConveyorCommands c1Commands;
    IConveyorCommands c2Commands;
    ISensorProvider c1Sensors;
    IRobotCommands r4Commands;

    ConveyorBox part1;
    //ConveyorBox part2;
    int cont = 0;

    @Override
    public void onInit() {
        c1Commands = useSkill(IConveyorCommands.class, "C11_Quality");
        c2Commands = useSkill(IConveyorCommands.class, "C12_KO");
        c1Sensors = useSkill(ISensorProvider.class, "C11_Quality");
        c1Sensors.registerOnSensors(this::onSensori2, "s2");
        r4Commands = useSkill(IRobotCommands.class, "R4");
    }

    private void onSensori2(SensorCatch t) {
        System.out.println("Got event" + t.sensorName);
        part1 = t.box;
        List<ConveyorBox> boards = part1.entity.getProperty("boards");
        schedule.startSerial();
        {
//            if (t.box.entity.getProperty("dif").equals(1)) {
            c1Commands.lock(t.box);
            schedule.waitTime(500);
            
            for (ConveyorBox board : boards) {
                if (board.entity.getProperty("defective")) {
                    r4Commands.move(BoxUtils.targetOffset(board, 0, 0, BoxUtils.zSize(board) + 100, 0, 0, 0), 1000);
                    r4Commands.moveLinear(BoxUtils.targetTop(board), 1000);
                    r4Commands.pick(board.entity);
//                    r4Commands.moveLinear(BoxUtils.targetTop(part1), t.box.cF, 2000);
                    r4Commands.move(driver.getFrameTransform("Frames.Qualita"), 2000);
                    r4Commands.release();
                    c2Commands.insert(board);
                }
            }
            r4Commands.home();
            c1Commands.release(t.box);
        }
        schedule.end();

    }

}

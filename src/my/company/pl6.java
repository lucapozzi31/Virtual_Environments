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

/**
 *
 * @author ciavr
 */
public class pl6 extends ProgrammableLogics {

    IConveyorCommands c1Commands;
    IConveyorCommands c2Commands;
    ISensorProvider c1Sensors;
    ISensorProvider c3Sensors;
    IRobotCommands r4Commands;

    ConveyorBox part1;
    //ConveyorBox part2;
    int cont = 0;

    @Override
    public void onInit() {
        c1Commands = useSkill(IConveyorCommands.class, "C11_Quality");
        c1Sensors = useSkill(ISensorProvider.class, "C11_Quality");
        c1Sensors.registerOnSensors(this::onSensori, "s1");
        c1Sensors.registerOnSensors(this::onSensori2, "s2");
        r4Commands = useSkill(IRobotCommands.class, "r4");
    }

    void onSensori(SensorCatch t) {
        System.out.println("Got event" + t.sensorName);
        part1 = t.box;

        schedule.startSerial();
        {
            c1Commands.lock(t.box);
            schedule.waitTime(4000);//per simulare il controllo qualità 
            c1Commands.release(part1);
            //c1Commands.release(part2);
            cont = 0;
        }
        schedule.end();

    }

    private void onSensori2(SensorCatch t) {
        System.out.println("Got event" + t.sensorName);
        part1 = t.box;

        schedule.startSerial();
        {
            c2Commands.lock(t.box);
            r4Commands.move(BoxUtils.targetOffset(t.box, 0, 0, BoxUtils.zSize(t.box) + 100, 0, 0, 0), 1000);
            r4Commands.moveLinear(BoxUtils.targetTop(t.box), 1000);
            r4Commands.pick(t.box.entity);
            c2Commands.remove(t.box);
            r4Commands.moveLinear(BoxUtils.targetTop(part1), t.box.cF, 2000);
            r4Commands.release();
            schedule.attach(t.box.entityId, part1.entityId);
            c1Commands.release(part1);

        }
        schedule.end();

    }

}

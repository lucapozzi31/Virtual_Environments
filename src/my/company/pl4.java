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
public class pl4 extends ProgrammableLogics {

    IConveyorCommands c1Commands;
    IConveyorCommands c2Commands;
    //IConveyorCommands c3Commands;
    ISensorProvider c1Sensors;
    ISensorProvider c3Sensors;
    //ISource p2cmd;
    //ISource p3cmd;

    ConveyorBox part1;
    ConveyorBox part2;
    int cont = 0;

    @Override
    public void onInit() {
        c1Commands = useSkill(IConveyorCommands.class, "f1");
        //c2Commands = useSkill(IConveyorCommands.class, "f2");
        c1Sensors = useSkill(ISensorProvider.class, "f1");
        c1Sensors.registerOnSensors(this::onSensori, "s1");
        c1Sensors.registerOnSensors(this::onSensori2, "s2");
        //c3Sensors = useSkill(ISensorProvider.class, "f2");
        //c3Sensors.registerOnSensors(this::onSensori3, "s2");
        //p2cmd= useSkill(ISource.class, "p2");
        //p3cmd= useSkill(ISource.class, "p3");
    }

    void onSensori(SensorCatch t) {
        System.out.println("Got event" + t.sensorName);
        part1 = t.box;

        if (cont == 0) {
            cont = 1;
        } else {
            schedule.startSerial();
            {
                c1Commands.lock(t.box);
                schedule.waitTime(3000);
                c1Commands.release(part1);
                c1Commands.release(part2);
                cont = 0;
            }
            schedule.end();

        }
    }

    private void onSensori2(SensorCatch t) {
        System.out.println("Got event" + t.sensorName);
        part2 = t.box;
        if (cont == 1) {
            schedule.startSerial();
            {
                c1Commands.lock(t.box);

            }
            schedule.end();
        }
    }

}

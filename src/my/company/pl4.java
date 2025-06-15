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
import com.ttsnetwork.modules.standard.PLGlobalState;
import com.ttsnetwork.modules.standard.ProgrammableLogics;
import com.ttsnetwork.modulespack.conveyors.ConveyorBox;
import com.ttsnetwork.modulespack.conveyors.SensorCatch;

/**
 *
 * @author ciavr
 */
public class pl4 extends ProgrammableLogics {

    IConveyorCommands c1Commands;
    ISensorProvider c1Sensors;

    ConveyorBox part1;
    ConveyorBox part2;
    int cont = 0;

    @Override
    public void onInit() {
        c1Commands = useSkill(IConveyorCommands.class, "C8_Forno2");
        c1Sensors = useSkill(ISensorProvider.class, "C8_Forno2");
        c1Sensors.registerOnSensors(this::onSensori, "s1");

    }

    void onSensori(SensorCatch t) {
        System.out.println("Got event" + t.sensorName);
        part1 = t.box;

        schedule.startSerial();
        {
            c1Commands.lock(t.box);
            schedule.waitTime(160000);
            c1Commands.release(part1);
        }
        schedule.end();

    }

}

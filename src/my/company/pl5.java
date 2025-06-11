/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package my.company;

import com.ttsnetwork.modules.standard.IConveyorCommands;
import com.ttsnetwork.modules.standard.ISensorProvider;
import com.ttsnetwork.modules.standard.SimpleStateVar;
import com.ttsnetwork.modules.standard.StateMachine;
import com.ttsnetwork.modulespack.conveyors.ConveyorBox;
import com.ttsnetwork.modulespack.conveyors.SensorCatch;

/**
 *
 * @author ciavr
 */
public class pl5 extends StateMachine {

    IConveyorCommands c1Commands;
    IConveyorCommands c2Commands;
    ISensorProvider c1Sensors;
    ISensorProvider c2Sensors;
    ISensorProvider c3Sensors;

    ConveyorBox partA;
//    private int counter;
    private int counterS2;
    private int counterS3;

    @Override
    public void onInit() {
        c1Commands = useSkill(IConveyorCommands.class, "C5_Forno1");
        c1Sensors = useSkill(ISensorProvider.class, "C5_Forno1");
        c1Sensors.registerOnSensors(this::onS1, "s1");
        c1Sensors.registerOnSensors(this::onS2, "s2");
        c1Sensors.registerOnSensors(this::onS3, "s3");

    }

    public void onStart() {
        switchState(100);
    }

    public SimpleStateVar boxEntering = new SimpleStateVar();
    public SimpleStateVar boxPos2 = new SimpleStateVar();
    public SimpleStateVar boxPos3 = new SimpleStateVar();
    public SimpleStateVar processComplete = new SimpleStateVar();

    public void state_100() {
        if (boxEntering.read() != null) {
            ConveyorBox box = boxEntering.readAndForget();
            schedule.startSerial();
            c1Commands.release(box);
            schedule.end();
            switchState(200);
        }
    }

    public void state_200() {
        if (boxEntering.read() != null) {
            ConveyorBox box = boxEntering.readAndForget();
            schedule.startSerial();
            c1Commands.release(box);
            schedule.end();
            switchState(300);
        }
    }

    public void state_300() {
        if (boxPos2.read() != null && boxPos3.read() != null) {
            processComplete.write(false);
            cook();
            switchState(400);
        }
    }

    public void state_400() {
        if (processComplete.readBoolean()) {
            switchState(100);
        }
    }

    void cook() {
        schedule.startSerial();
        schedule.waitTime(400000);
        c1Commands.release(boxPos2.readAndForget());
        c1Commands.release(boxPos3.readAndForget());
        setVar(processComplete, true);
        schedule.end();
    }

    void onS1(SensorCatch sc) {
        schedule.startSerial();
        c1Commands.lock(sc.box);
        setVar(boxEntering, sc.box);
        schedule.end();
    }

    void onS2(SensorCatch sc) {
//        if (counter % 2 == 1) {
        counterS2++;
        if (counterS2 % 2 == 0) {
            schedule.startSerial();
            c1Commands.lock(sc.box);
            setVar(boxPos2, sc.box);
            schedule.end();
        }

    }

    void onS3(SensorCatch sc) {
        counterS3++;
        if (counterS3 % 2 == 1) {
            schedule.startSerial();
            c1Commands.lock(sc.box);
            setVar(boxPos3, sc.box);
            schedule.end();
        }
    }

}

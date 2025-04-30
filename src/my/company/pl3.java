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
import com.ttsnetwork.modules.standard.IShuttle;
import com.ttsnetwork.modules.standard.ISource;
import com.ttsnetwork.modules.standard.ProgrammableLogics;
import com.ttsnetwork.modulespack.conveyors.SensorCatch;

//Programmazione del robot nella prima postazione di assemblaggio
public class pl3 extends ProgrammableLogics {

    // IConveyorCommands c1commands;
    IConveyorCommands ciniz1commands;
    ISensorProvider ciniz1sensors;
    IRobotCommands r3commands;

    IShuttle shuttle1;
     ISource ps1_c;

    @Override
    public void onInit() {

        ciniz1commands = useSkill(IConveyorCommands.class, "ciniz1");
        ciniz1sensors = useSkill(ISensorProvider.class, "ciniz1");
        ciniz1sensors.registerOnSensors(this::onSensori);
        r3commands = useSkill(IRobotCommands.class, "r3");

        shuttle1 = useSkill(IShuttle.class, "S1");
        shuttle1.registerOnPosition(1, this::onPosition_s1_1);
        shuttle1.registerOnPosition(2, this::onPosition_s1_2);
        ps1_c = useSkill(ISource.class, "PS1_C");
      

    }

    
    @Override
    public void onStart() {
        schedule.startSerial();
        //ps1_c.create(null);
        schedule.end();
    }
    private void onSensori(SensorCatch t) {

        schedule.startSerial();
        {
            //Arriva la scatola, la prendo
            ciniz1commands.lock(t.box);
            r3commands.move(driver.getFrameTransform("ciniz1group.f1"), 2000);
            r3commands.move(BoxUtils.targetOffset(t.box, 0, 0, 100, 0, 0, 0), 1000);
            r3commands.pick(t.box.entity);

            //Decido dove mettere la scatola in base alla saturazione macchina
            //Vado su R1
            r3commands.move(driver.getFrameTransform("ciniz1group.f2"), 2000);
            r3commands.release();
            //ps1_c.create(null);
            //ps1_c.insert(t.box);

            
            /*Vado su R2
            r3commands.move(driver.getFrameTransform("ciniz1group.f3"), 2000);
            r3commands.move(BoxUtils.targetOffset(t.box, 0, 0, 100, 0, 0, 0), 1000);
            r3commands.release();*/
            //Torno a destinazione

            /*ciniz1commands.remove(t.box);
            r3commands.move(driver.getFrameTransform("ciniz1group.f2"), 5000);
            r3commands.move(driver.getFrameTransform("C2.startFrame"), t.box.cF, 1500);
            r3commands.release();
            sh1commands.insert(t.box);*/
            r3commands.home();
        }

        schedule.end();

    }

    private void onPosition_s1_1(SensorCatch t) {
        schedule.startSerial();
        shuttle1.shuttle();
        schedule.end();
    }
    
        private void onPosition_s1_2(SensorCatch t) {
        schedule.startSerial();
        //ciclo di assemblaggio
        
        
        
        shuttle1.shuttle();
        schedule.end();
    }
}

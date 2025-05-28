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
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

/**
 *
 * @author ciavr
 */
public class Qualita extends ProgrammableLogics {
    
    IConveyorCommands c1Commands;
    ISensorProvider c1Sensors;
    
    ConveyorBox part1;
    //ConveyorBox part2;
    int cont = 0;
    
    UniformRealDistribution distA;
    UniformRealDistribution distB;
    
    @Override
    public void onInit() {
        c1Commands = useSkill(IConveyorCommands.class, "C11_Quality");
        c1Sensors = useSkill(ISensorProvider.class, "C11_Quality");
        c1Sensors.registerOnSensors(this::onSensori, "s1");
        
        distA = model.getRandomGenerator().getUniformRealDistribution(0, 100);
        distB = model.getRandomGenerator().getUniformRealDistribution(0, 100);
    }
//ferma solo la scatola e controlla 

    void onSensori(SensorCatch t) {
        System.out.println("Got event" + t.sensorName);
        part1 = t.box;
        List<ConveyorBox> boards = part1.entity.getProperty("boards");
        
        long wt = boards.size() * 1000;
        
        for (ConveyorBox board : boards) {
            if (board.entity.getProperty("rfid").equals("P001")) {
                double ext = distA.sample();
                board.entity.setProperty("defective", ext <= 1);
            } else {
                double extB = distB.sample();
                board.entity.setProperty("defective", extB <= 2);
                
            }
            
        }
        
        schedule.startSerial();
        {
            c1Commands.lock(t.box);
            schedule.waitTime(wt);//per simulare il controllo qualità 
            c1Commands.release(part1);
            //c1Commands.release(part2);
            cont = 0;
        }
        schedule.end();
        
    }
    
}

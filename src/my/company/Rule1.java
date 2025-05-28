/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package my.company;

import com.ttsnetwork.modules.standard.IConveyorCommands;
import com.ttsnetwork.modules.standard.ISensorProvider;
import com.ttsnetwork.modules.standard.SplitRule;
import com.ttsnetwork.modulespack.conveyors.ConveyorBox;

/**
 *
 * @author ciavr
 */
public class Rule1 extends SplitRule {

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
    public int select(ConveyorBox cb) {
        return 0;
    }
    
    
    
    
    
}

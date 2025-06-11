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

    private int counter;

    @Override
    public int select(ConveyorBox cb) {
        int rem = counter % 3;
        counter++;
        if (rem < 2) {
            return 0;
        }else{
            return 2;
        }
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package my.company;

import com.ttsnetwork.modules.standard.SplitRule;
import com.ttsnetwork.modulespack.conveyors.ConveyorBox;

/**
 *
 * @author ciavr
 */
public class Rule1 extends SplitRule{

    @Override
    public int select(ConveyorBox cb) {
        
        return 2;
    }
    
}

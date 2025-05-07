// LineInfo.java
package my.company;

import com.ttsnetwork.modules.standard.PLGlobalState;

public class LineInfo implements PLGlobalState {

    public volatile boolean assemblyDoneStation1 = true; // ok a caricare un nuovo pezzo
    public volatile boolean shuttle1Busy         = false;
}

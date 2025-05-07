package my.company;

import com.ttsnetwork.modules.standard.PLGlobalState;


//Variabili condivise tra le diverse Programmable Logics.
 
public class LineInfo implements PLGlobalState {

    // Flag di assemblaggio
    public boolean assemblaggioCompletatoStazione1 = false;
    public boolean assemblaggioCompletatoStazione2 = false;

    // Stato shuttle
    public boolean shuttle1InPosizioneIniziale = true;
    public boolean shuttle2InPosizioneIniziale = true;

    // Contatori pezzi
    public int pezziSuShuttle1 = 0;
    public int pezziSuShuttle2 = 0;
}

package ch.zhaw.gpi.twitterreview.delegates;

import javax.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * Implementation des Service Task "Tweet senden"
 * 
 * @author scep
 */
@Named("sendTweetAdapter")
public class SendTweetDelegate implements JavaDelegate {

    /**
     * Mockt das Senden eines Tweeets
     * 
     * 1. Die Prozessvariable tweetContent wird ausgelesen
     * 2. Dieser Text wird in der Console ausgegeben
     * 
     * @param de            Objekt, welches die Verknüpfung zur Process Engine und zur aktuellen Execution enthält
     * @throws Exception
     */
    @Override
    public void execute(DelegateExecution de) throws Exception {
        String tweetContent = (String) de.getVariable("tweetContent");
        System.out.println("!!!!!!!!!!!!!!!! Folgender Tweet wird veröffentlicht: " + tweetContent);
    }
    
}

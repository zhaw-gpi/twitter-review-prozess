package ch.zhaw.gpi.twitterreview.delegates;

import ch.zhaw.gpi.twitterreview.services.EmailService;
import javax.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation des Send Task "Mitarbeiter benachrichtigen"
 * 
 * @author scep
 */
@Named("notifyEmployeeAdapter")
public class NotifyEmployeeDelegate implements JavaDelegate {
    
    // Verdrahten des Mail-Services 
    @Autowired
    private EmailService emailService;

    /**
     * Mockt das Senden einer Benachrichtigung per Mail
     * 
     * 1. Die benötigten Prozessvariablen auslesen
     * 2. Die E-Mail-Nachricht zusammenstellen
     * 3. Die E-Mail in der Konsole ausgeben
     * 
     * @param de
     * @throws Exception
     */
    @Override
    public void execute(DelegateExecution de) throws Exception {
        // Prozessvariablen auslesen
        String email = (String) de.getVariable("email");
        String tweetContent = (String) de.getVariable("tweetContent");
        String checkResult = (String) de.getVariable("checkResult");
        String checkResultComment = (String) de.getVariable("checkResultComment");
        Object mailMainPart = de.getVariable("mailMainPart");
        
        // Die E-Mail-Nachricht zusammenbauen
        String mailHauptteil;
        
        if(mailMainPart instanceof String){
            mailHauptteil = (String) mailMainPart;
        } else if(checkResult.equals("rejected")){
            mailHauptteil = "Leider wurde diese Tweet-Anfrage abgelehnt mit " +
                    "folgender Begründung:\n" + checkResultComment;
        } else {
            mailHauptteil = "Dein Tweet wurde geposted. Herzlichen Dank für Deinen Beitrag.";
        }
        
        // Mail-Text zusammenbauen
        String mailBody = "Hallo Mitarbeiter\n\n" + "Du hast folgenden Text zum " +
                "Veröffentlichen als Tweet vorgeschlagen:\n" + tweetContent + "\n\n" +
                mailHauptteil + "\n\n" + "Deine Kommunikationsabteilung";
        
        // Mail über Mailservice versenden
        emailService.sendSimpleMail(email, "Neuigkeiten zu Ihrer Tweet-Anfrage", mailBody);
    }
    
}

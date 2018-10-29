package ch.zhaw.gpi.twitterreview.services;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.ApiException;
import org.springframework.social.twitter.api.TimelineOperations;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Service;

/**
 * Service-Klasse für die Kommunikation mit der Twitter API
 * 
 * @Service stellt sicher, dass diese Klasse beim Starten der Applikation als
 * Bean generiert wird (wie bei @Component und @Bean), aber als Service gekennzeichnet ist
 * 
 * @author scep
 */
@Service
public class TwitterService {
    // Variable für ein Twitter-Objekt, welches die Verbindung zur Twitter API beinhaltet
    private Twitter twitter;
    
    // Variable für ein TimelineOperations-Objekt (Sub-API für Timeline lesen und Tweets posten)
    private TimelineOperations timelineOperations;

    // Werte aus twitter.properties für die Authentifizierung bei der Twitter API
    @Value("${twitter.consumerKey}")
    private String consumerKey;
    @Value("${twitter.consumerSecret}")
    private String consumerSecret;
    @Value("${twitter.accessToken}")
    private String accessToken;
    @Value("${twitter.accessTokenSecret}")
    private String accessTokenSecret;
    
    /**
     * Verbindung zur Twitter API aufbauen beim Initialisieren dieser Klasse
     * 
     * @PostConstruct (und die init-Bezeichnung) stellen sicher,
     * dass die Methode erst ausgeführt wird, wenn die Klasse bereits konstruiert
     * wurde und daher z.B. @Value bereits ausgeführt wurde
     */
    @PostConstruct
    private void init() {
        // Versuchen, die Verbindung mit Twitter aufzubauen
        try {
            // Anmelden bei Twitter API über die Zugangsdaten
            twitter = new TwitterTemplate(consumerKey, consumerSecret, accessToken, accessTokenSecret);

            // TimelineOperations-Objekt der entsprechenden Variable zuweisen
            timelineOperations = twitter.timelineOperations();

            // In Konsole erfolgreiche Anmeldung ausgeben
            System.out.println("Anmeldung bei Twitter erfolgreich. Angemeldeter Benutzer: "
                    + twitter.userOperations().getScreenName());
        } catch (ApiException e) {
            // Falls fehlgeschlagen, dann Fehlermeldung in Konsole ausgeben
            System.err.println("Anmeldung bei Twitter fehlgeschlagen. Meldung: " + e.getLocalizedMessage());
        }
    }
    
    /**
     * Postet einen neuen Tweet auf Twitter (= updateStatus)
     *
     * @param statusText Der zu postende Text
     * @throws java.lang.Exception
     */
    public void updateStatus(String statusText) throws Exception {
        // Versucht, den Tweet zu posten oder "behandelt" Fehler durch Ausgabe von Meldungen in der Konsole
        try {
            // Tweet posten
            timelineOperations.updateStatus(statusText);

            // Als Bestätigung in der Output-Konsole den Text des letzten Tweets zurückgeben
            System.out.println("---------- Letzter geposteter Tweet: " + 
                    timelineOperations.getUserTimeline(1).get(0).getText());            
        } catch (ApiException e) {
            // Fehler ausgeben in Konsole (vereinfacht das Testen)
            System.err.println("Tweet posten fehlgeschlagen: " + e.getLocalizedMessage());

            // Fehler an aufrufende Methode zurück geben
            throw new Exception("Tweet posten fehlgeschlagen", e);
        }
    }
}

package ch.zhaw.gpi.twitterreview;

import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests für den Twitter-Review-Prozess
 * 
 * Getestet werden die drei Varianten des Prüfergebnisses (genehmigt, abgelehnt, zu überarbeiten)
 * 
 * Abkürzungen in den Zeilenkommentaren:
 *   ST: Steuerung der Engine
 *   BP: Behauptung (Assertion) prüfen
 * 
 * Code angelehnt aus:
 * - https://github.com/camunda/camunda-bpm-assert
 * - https://github.com/camunda/camunda-bpm-process-test-coverage/tree/master/test/src/test/java/org/camunda/bpm/extension/process_test_coverage/spring
 * - https://github.com/camunda/camunda-bpm-platform/tree/master/engine-spring/src/test/java/org/camunda/bpm/engine/spring
 * - https://docs.camunda.org/manual/7.9/user-guide/spring-framework-integration/testing/
 * - https://docs.camunda.org/manual/7.9/user-guide/process-engine/the-job-executor/#job-executor-in-a-unit-test
 *
 * @author scep, heip
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class TwitterReviewProcessTest {

    // In den Tests verwendete Konstanten setzen
    private static final String PROCESS_DEFINITION_KEY = "VerarbeitungVonTweetAnfragen";
    
    private static final String VAR_NAME_EMAIL = "email";
    private static final String VAR_NAME_ALIAS = "alias";
    private static final String VAR_NAME_TWEET_CONTENT = "tweetContent";
    private static final String VAR_NAME_CHECK_RESULT = "checkResult";      
    
    private static final String BPMN_ELEMENT_TWEET_PRUEFEN = "TweetAnfragePruefen";
    private static final String BPMN_ELEMENT_PRUEFERGEBNIS = "Pruefergebnis";
    private static final String BPMN_ELEMENT_TWEET_SENDEN = "TweetSenden";
    private static final String BPMN_ELEMENTMITARBEITER_BENACHRICHTIGEN = "MitarbeiterBenachrichtigen";
    
    private static final String TEST_EMAIL = "scep@zhaw.ch";
    private static final String TEST_ALIAS = "scep";
    private static final String TEST_TWEET_CONTENT = "Was für ein toller Tweet";
    
    /**
     * Die Process Engine wird in der TestConfiguration-Klasse gebaut dank der 
     * Annotation @ContextConfiguration(classes = {TestConfiguration.class}) oben
     * 
     * Die benötigten Services der Process Engine werden per 
     * Autowiring der vorliegenden Klasse zur Verfügung gestellt
     */    
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;

    /**
     * Happy Path testen (Tweet-Content wird genehmigt)
     * 
     * Der Test erfolgt ausführlich, indem fast jeder Schritt separat
     * geprüft wird, um die Mächtigkeit der BPM Asserts zu zeigen
     */
    @Test
    public void testHappyPath() {
        // ST: Variablen setzen, welche normalerweise der Benutzer im Startformular eingibt
        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put(VAR_NAME_EMAIL, TEST_EMAIL);
        processVariables.put(VAR_NAME_TWEET_CONTENT, TEST_TWEET_CONTENT);

        // ST: Neue Prozessinstanz mit diesen Variablen starten
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, processVariables);

        // BP: Neue Prozessinstanz ist gestartet
        assertThat(processInstance).isStarted();

        // BP: Mitarbeiter-Kürzel-Variable ist gesetzt und hat den erwarteten Wert
        assertThat(processInstance).variables().containsEntry(VAR_NAME_ALIAS, TEST_ALIAS);

        // BP: Ein unerledigter User Task TweetAnfragePruefen besteht
        assertThat(processInstance).task().hasDefinitionKey(BPMN_ELEMENT_TWEET_PRUEFEN);

        // ST: Den einzig offenen Task erledigen mit einem positiven Prüfergebnis
        complete(task(processInstance), withVariables(VAR_NAME_CHECK_RESULT, "accepted"));

        // BP: Das Gateway 'Prüfergebnis' wurde durchschritten
        assertThat(processInstance).hasPassed(BPMN_ELEMENT_PRUEFERGEBNIS);

        // ST: Den asynchronen Job TweetSenden ausführen
          // Hierzu zunächst nach dem einen offenen Job suchen. Falls es keinen oder mehrere gibt, dann gibt es eine Exception
        Job job = managementService.createJobQuery().activityId(BPMN_ELEMENT_TWEET_SENDEN).singleResult();
          // Dann diesen Job ausführen
        managementService.executeJob(job.getId());
        
        // BP: Die Aufgabe 'Tweet senden' wurde durchschritten
        assertThat(processInstance).hasPassed(BPMN_ELEMENT_TWEET_SENDEN);
        
        // ST: Den asynchronen Job MitarbeiterBenachrichtigen ausführen
          // Hierzu zunächst nach dem einen offenen Job suchen. Falls es keinen oder mehrere gibt, dann gibt es eine Exception
        job = managementService.createJobQuery().activityId(BPMN_ELEMENTMITARBEITER_BENACHRICHTIGEN).singleResult();
          // Dann diesen Job ausführen
        managementService.executeJob(job.getId());

        // BP: Die Prozessinstanz ist beendet        
        assertThat(processInstance).isEnded();
    }

    @Test
    public void testRejection() {
        // ST: Variablen setzen, welche normalerweise der Benutzer im Startformular eingibt
        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put(VAR_NAME_EMAIL, TEST_EMAIL);
        processVariables.put(VAR_NAME_TWEET_CONTENT, TEST_TWEET_CONTENT);

        // ST: Neue Prozessinstanz mit diesen Variablen starten
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, processVariables);

        // ST: Den einzig offenen Task erledigen mit einem ablehnenden Prüfergebnis
        complete(task(processInstance), withVariables(VAR_NAME_CHECK_RESULT, "rejected"));
        
        // ST: Den asynchronen Job MitarbeiterBenachrichtigen ausführen
          // Hierzu zunächst nach dem einen offenen Job suchen. Falls es keinen oder mehrere gibt, dann gibt es eine Exception
        Job job = managementService.createJobQuery().activityId(BPMN_ELEMENTMITARBEITER_BENACHRICHTIGEN).singleResult();
          // Dann diesen Job ausführen
        managementService.executeJob(job.getId());

        // BP: Die Prozessinstanz ist beendet        
        assertThat(processInstance).isEnded();
    }

    @Test
    public void testRevision() {
        
        // ST: Variablen setzen, welche normalerweise der Benutzer im Startformular eingibt
        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put(VAR_NAME_EMAIL, TEST_EMAIL);
        processVariables.put(VAR_NAME_TWEET_CONTENT, TEST_TWEET_CONTENT);

        // ST: Neue Prozessinstanz mit diesen Variablen starten
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, processVariables);

        // ST: Den einzig offenen Task erledigen mit einem Prüfergebnis "zu überarbeiten"
        complete(task(processInstance), withVariables(VAR_NAME_CHECK_RESULT, "revisable"));
        
        // ST: Den einzig offenen Task erledigen (theoretisch mit einem neuen TweetContent, aber da ja kein Benutzer da ist, der diesen liest, sparen wir uns dies)
        complete(task(processInstance));
        
        // BP: Ein unerledigter User Task TweetAnfragePruefen besteht
        assertThat(processInstance).task().hasDefinitionKey(BPMN_ELEMENT_TWEET_PRUEFEN);
        
        // Alles weitere interessiert uns nicht, da schon in den anderen Testfällen abgedeckt
    }
}

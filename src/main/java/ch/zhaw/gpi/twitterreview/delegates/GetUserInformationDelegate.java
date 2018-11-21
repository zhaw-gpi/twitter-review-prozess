package ch.zhaw.gpi.twitterreview.delegates;

import ch.zhaw.gpi.twitterreview.resources.User;
import ch.zhaw.gpi.twitterreview.services.UserService;
import javax.inject.Named;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author scep
 */
@Named(value = "getUserInformationAdapter")
public class GetUserInformationDelegate implements JavaDelegate {
    
    @Autowired
    private UserService userService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String anfrageStellenderBenutzer = (String) execution.getVariable("anfrageStellenderBenutzer");
        
        User user = userService.getUser(anfrageStellenderBenutzer);
        
        if(user == null){
            throw new BpmnError("UserNotFound", "Kein Benutzer " + anfrageStellenderBenutzer + " gefunden");
        }
        
        execution.setVariable("firstName", user.getFirstName());
        execution.setVariable("email", user.geteMail());
    }
    
}

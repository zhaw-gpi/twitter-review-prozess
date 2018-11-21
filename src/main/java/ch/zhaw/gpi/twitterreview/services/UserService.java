package ch.zhaw.gpi.twitterreview.services;

import ch.zhaw.gpi.twitterreview.resources.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author scep
 */
@Component
public class UserService {
    
    private final RestTemplate restTemplate;
    
    @Value(value = "${userservice.endpoint}")
    private String userServiceEndpoint;
    
    public UserService(){
        restTemplate = new RestTemplate();
    }
    
    public User getUser(String userName){
        try{
            User user = restTemplate.getForObject(userServiceEndpoint + "/users/{userName}", User.class, userName);
            return user;
        } catch(HttpClientErrorException httpClientErrorException){
            if(httpClientErrorException.getStatusCode() == HttpStatus.NOT_FOUND){
                return null;
            } else {
                throw httpClientErrorException;
            }
        }       
    }
}

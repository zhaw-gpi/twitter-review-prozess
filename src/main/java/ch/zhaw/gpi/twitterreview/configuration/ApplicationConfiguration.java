package ch.zhaw.gpi.twitterreview.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Verschiedene Einstellungen für die Spring-Applikation
 * 
 * @Configuration stellt sicher, dass Spring diese Klasse als Einstellungs-
 * Klasse erkennt und damit beim Starten berücksichtigt
 * 
 * @PropertySource("classpath:twitter.properties") stellt sicher, dass die Werte
 * aus dieser Datei über @Value-Annotationen ausgelesen werden können
 * 
 * @author scep
 */
@Configuration
@PropertySource("classpath:twitter.properties")
public class ApplicationConfiguration {
    
}

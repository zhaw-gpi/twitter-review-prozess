package ch.zhaw.gpi.twitterreview;

import javax.sql.DataSource;

import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * Konfigurations-Klasse, um eine neue In-Memory-ProcessEngine zu konfigurieren
 *
 * Adaptiert nach dem XML-Pendant aus
 * https://github.com/camunda/camunda-bpm-platform/blob/master/engine-spring/src/test/resources/org/camunda/bpm/engine/spring/test/junit4/springTypicalUsageTest-context.xml
 *
 * @author scep, heip
 */
@Configuration
@ComponentScan("ch.zhaw.gpi")
public class TestConfiguration {

    /**
     * Erstellt eine Datenquelle In-Memory basierend auf H2
     *
     * @return
     */
    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.h2.Driver.class);
        dataSource.setUrl("jdbc:h2:mem:camunda-test;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    /**
     * Erstellt einen Transaktionsmanager, was scheinbar im Spring-Kontext relevant ist
     * 
     * @return
     */
    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    /**
     * Konfiguriert eine Process Engine im Spring-Kontext
     * 
     * - Setzt die oben definierte Datenquelle und den Transaktionsmanager
     * - Deaktiviert den Camunda JobExecutor und den MetricsReporter
     * 
     * @return
     */
    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration() {
        SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();

        processEngineConfiguration.setDataSource(dataSource());
        processEngineConfiguration.setTransactionManager(transactionManager());
        processEngineConfiguration.setDatabaseSchemaUpdate("true");

        processEngineConfiguration.setJobExecutorActivate(false);
        processEngineConfiguration.setDbMetricsReporterActivate(false);

        return processEngineConfiguration;
    }

    /**
     * Baut die Process Engine unter Verwendung der oben definierten Konfiguration
     * 
     * @return
     */
    @Bean
    public ProcessEngineFactoryBean processEngine() {
        ProcessEngineFactoryBean processEngine = new ProcessEngineFactoryBean();
        processEngine.setProcessEngineConfiguration(processEngineConfiguration());
        return processEngine;
    }

}

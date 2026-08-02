package net.jojoaddison.consultancy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.jojoaddison.consultancy.config.AsyncSyncConfiguration;
import net.jojoaddison.consultancy.config.EmbeddedSQL;
import net.jojoaddison.consultancy.config.JacksonConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        JojoaddisonApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        net.jojoaddison.consultancy.config.JacksonHibernateConfiguration.class,
    }
)
@EmbeddedSQL
public @interface IntegrationTest {}

package net.jojoaddison.consultancy.config;

import java.time.Duration;
import org.ehcache.config.builders.*;
import org.ehcache.jsr107.Eh107Configuration;
import org.hibernate.cache.jcache.ConfigSettings;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        var ehcache = jHipsterProperties.getCache().getEhcache();

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                Object.class,
                Object.class,
                ResourcePoolsBuilder.heap(ehcache.getMaxEntries())
            )
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                .build()
        );
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, net.jojoaddison.consultancy.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, net.jojoaddison.consultancy.repository.UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, net.jojoaddison.consultancy.domain.User.class.getName());
            createCache(cm, net.jojoaddison.consultancy.domain.Authority.class.getName());
            createCache(cm, net.jojoaddison.consultancy.domain.User.class.getName() + ".authorities");
            createCache(cm, net.jojoaddison.consultancy.domain.Client.class.getName());
            createCache(cm, net.jojoaddison.consultancy.domain.Client.class.getName() + ".projects");
            createCache(cm, net.jojoaddison.consultancy.domain.Client.class.getName() + ".tickets");
            createCache(cm, net.jojoaddison.consultancy.domain.Client.class.getName() + ".quotes");
            createCache(cm, net.jojoaddison.consultancy.domain.Project.class.getName());
            createCache(cm, net.jojoaddison.consultancy.domain.Project.class.getName() + ".milestones");
            createCache(cm, net.jojoaddison.consultancy.domain.Milestone.class.getName());
            createCache(cm, net.jojoaddison.consultancy.domain.Ticket.class.getName());
            createCache(cm, net.jojoaddison.consultancy.domain.ServiceItem.class.getName());
            createCache(cm, net.jojoaddison.consultancy.domain.Quote.class.getName());
            createCache(cm, net.jojoaddison.consultancy.domain.Quote.class.getName() + ".lines");
            createCache(cm, net.jojoaddison.consultancy.domain.QuoteLine.class.getName());
            createCache(cm, net.jojoaddison.consultancy.domain.Course.class.getName());
            createCache(cm, net.jojoaddison.consultancy.domain.TeamMember.class.getName());
            createCache(cm, net.jojoaddison.consultancy.domain.Lead.class.getName());
            // jhipster-needle-ehcache-add-entry
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }
}

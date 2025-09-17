package org.example.elearningbe.config;

import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.integration.redis.RedisToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Slf4j
@Configuration
@EnableRedisRepositories
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        log.info("Configuring Redis connection factory with host: {} and port: {}", redisHost, redisPort);
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        return new JedisConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        log.info("Configuring Redis template");
        return  redisTemplate;
    }

    @Bean
    public RedisTemplate<String, RedisToken> redisTokenRedisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, RedisToken> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }

}

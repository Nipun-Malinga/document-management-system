package com.nipun.system.shared.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()
                ));

        return RedisCacheManager
                .builder(connectionFactory)
                .cacheDefaults(configuration)
                .build();
    }

    @Bean
    public ApplicationRunner clearSelectedCache(CacheManager cacheManager) {
        return _ -> {
            clearCache(cacheManager.getCache("DOCUMENT_SESSION_CACHE"));
            clearCache(cacheManager.getCache("DOCUMENT_CONNECTED_USERS_CACHE"));
            clearCache(cacheManager.getCache("CONNECTED_USERS_CACHE"));
            clearCache(cacheManager.getCache("CONNECTED_SESSION_CACHE"));
            clearCache(cacheManager.getCache("DOCUMENT_USER_PERMISSION_CACHE"));
        };
    }

    private void clearCache(Cache cache) {
        if (cache != null) cache.clear();
    }
}

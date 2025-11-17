package com.sleekydz86.payment2v2.global.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

    private final ObjectMapper objectMapper;

    public RedisConfig() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 타입 정보 활성화: 역직렬화 시 정확한 타입으로 복원되도록 함
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();
        this.objectMapper.activateDefaultTyping(
                ptv,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .transactionAware()
                .build();

        return new ErrorHandlingCacheManager(redisCacheManager);
    }

    private static class ErrorHandlingCacheManager implements CacheManager {
        private final CacheManager delegate;
        private final NoOpCacheManager fallback;

        public ErrorHandlingCacheManager(CacheManager delegate) {
            this.delegate = delegate;
            this.fallback = new NoOpCacheManager();
        }

        @Override
        public Cache getCache(String name) {
            try {
                Cache cache = delegate.getCache(name);
                if (cache != null) {
                    return new ErrorHandlingCache(cache);
                }
                return fallback.getCache(name);
            } catch (Exception e) {
                log.warn("캐시 '{}' 조회 중 오류 발생, 캐시 없이 동작합니다: {}", name, e.getMessage());
                return fallback.getCache(name);
            }
        }

        @Override
        public Collection<String> getCacheNames() {
            try {
                return delegate.getCacheNames();
            } catch (Exception e) {
                log.warn("캐시 이름 목록 조회 중 오류 발생: {}", e.getMessage());
                return fallback.getCacheNames();
            }
        }
    }

    private static class ErrorHandlingCache implements Cache {
        private final Cache delegate;
        private final ObjectMapper objectMapper;

        public ErrorHandlingCache(Cache delegate) {
            this.delegate = delegate;
            this.objectMapper = new ObjectMapper();
            this.objectMapper.registerModule(new JavaTimeModule());
            this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // 타입 정보 활성화: 역직렬화 시 정확한 타입으로 복원되도록 함
            PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                    .allowIfSubType(Object.class)
                    .build();
            this.objectMapper.activateDefaultTyping(
                    ptv,
                    ObjectMapper.DefaultTyping.NON_FINAL,
                    JsonTypeInfo.As.PROPERTY);
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Object getNativeCache() {
            return delegate.getNativeCache();
        }

        @Override
        public ValueWrapper get(Object key) {
            try {
                ValueWrapper wrapper = delegate.get(key);
                if (wrapper != null) {
                    Object value = wrapper.get();

                    // Check for single LinkedHashMap object
                    // LinkedHashMap typically indicates deserialization without proper type
                    // information
                    // We evict it to force regeneration with correct type
                    if (value instanceof LinkedHashMap && !(value instanceof List)) {
                        log.warn("캐시에서 LinkedHashMap 감지 (단일 객체), 캐시 무효화: key={}, cache={}", key, getName());
                        try {
                            delegate.evict(key);
                        } catch (Exception evictError) {
                            log.debug("캐시 무효화 실패: key={}, error={}", key, evictError.getMessage());
                        }
                        return null;
                    }

                    // Check for LinkedHashMap in Lists
                    if (value instanceof List) {
                        List<?> list = (List<?>) value;
                        if (!list.isEmpty() && list.get(0) instanceof LinkedHashMap) {
                            log.warn("캐시에서 LinkedHashMap 감지 (리스트), 캐시 무효화: key={}, cache={}", key, getName());
                            try {
                                delegate.evict(key);
                            } catch (Exception evictError) {
                                log.debug("캐시 무효화 실패: key={}, error={}", key, evictError.getMessage());
                            }
                            return null;
                        }
                    }
                }
                return wrapper;
            } catch (ClassCastException e) {
                log.warn("캐시 역직렬화 오류 (타입 불일치), 캐시 무효화: key={}, error={}", key, e.getMessage());
                try {
                    delegate.evict(key);
                } catch (Exception evictError) {
                    log.debug("캐시 무효화 실패: key={}, error={}", key, evictError.getMessage());
                }
                return null;
            } catch (Exception e) {
                log.debug("캐시 조회 중 오류 발생 (무시됨): key={}, error={}", key, e.getMessage());
                return null;
            }
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            try {
                // Always get the raw value first to avoid ClassCastException from
                // delegate.get(key, type)
                // Never call delegate.get(key, type) directly as it may throw
                // ClassCastException
                ValueWrapper wrapper;
                try {
                    wrapper = delegate.get(key);
                } catch (ClassCastException e) {
                    log.warn("캐시 조회 중 ClassCastException 발생, 캐시 무효화: key={}, type={}, error={}",
                            key, type.getName(), e.getMessage());
                    try {
                        delegate.evict(key);
                    } catch (Exception evictError) {
                        log.debug("캐시 무효화 실패: key={}, error={}", key, evictError.getMessage());
                    }
                    return null;
                }

                if (wrapper == null) {
                    return null;
                }

                Object rawValue;
                try {
                    rawValue = wrapper.get();
                } catch (ClassCastException e) {
                    log.warn("캐시 값 추출 중 ClassCastException 발생, 캐시 무효화: key={}, type={}, error={}",
                            key, type.getName(), e.getMessage());
                    try {
                        delegate.evict(key);
                    } catch (Exception evictError) {
                        log.debug("캐시 무효화 실패: key={}, error={}", key, evictError.getMessage());
                    }
                    return null;
                }

                if (rawValue == null) {
                    return null;
                }

                // Check if the raw value is a LinkedHashMap when expecting a different type
                if (rawValue instanceof LinkedHashMap && !Map.class.isAssignableFrom(type)) {
                    log.warn("캐시에서 LinkedHashMap 감지 (단일 객체), 수동 변환 시도: key={}, expected={}, cache={}",
                            key, type.getName(), getName());
                    try {
                        // LinkedHashMap을 원하는 타입으로 변환
                        T convertedValue = objectMapper.convertValue(rawValue, type);
                        if (convertedValue != null) {
                            log.debug("LinkedHashMap 변환 성공: key={}, type={}", key, type.getName());
                            // 변환된 값을 다시 캐시에 저장
                            delegate.put(key, convertedValue);
                            return convertedValue;
                        }
                    } catch (Exception conversionError) {
                        log.warn("LinkedHashMap 변환 실패, 캐시 무효화: key={}, error={}", key,
                                conversionError.getMessage());
                    }
                    // 캐시 무효화하여 다음 호출 시 새로 생성되도록 함
                    try {
                        delegate.evict(key);
                    } catch (Exception evictError) {
                        log.debug("캐시 무효화 실패: key={}, error={}", key, evictError.getMessage());
                    }
                    return null;
                }

                // Check for LinkedHashMap in Lists
                if (rawValue instanceof List) {
                    List<?> list = (List<?>) rawValue;
                    if (!list.isEmpty() && list.get(0) instanceof LinkedHashMap) {
                        log.warn("캐시에서 LinkedHashMap 감지 (리스트), 캐시 무효화: key={}, cache={}", key, getName());
                        try {
                            delegate.evict(key);
                        } catch (Exception evictError) {
                            log.debug("캐시 무효화 실패: key={}, error={}", key, evictError.getMessage());
                        }
                        return null;
                    }
                }

                // If rawValue is already the correct type, cast and return it
                if (type.isInstance(rawValue)) {
                    return type.cast(rawValue);
                }

                // If not the correct type but not a LinkedHashMap, try conversion
                log.warn("캐시 타입 불일치 감지, 변환 시도: key={}, expected={}, actual={}",
                        key, type.getName(), rawValue.getClass().getName());
                try {
                    T convertedValue = objectMapper.convertValue(rawValue, type);
                    if (convertedValue != null) {
                        log.debug("타입 변환 성공: key={}, type={}", key, type.getName());
                        delegate.put(key, convertedValue);
                        return convertedValue;
                    }
                } catch (Exception conversionError) {
                    log.warn("타입 변환 실패, 캐시 무효화: key={}, error={}", key, conversionError.getMessage());
                }

                // If conversion failed, evict the cache entry
                try {
                    delegate.evict(key);
                } catch (Exception evictError) {
                    log.debug("캐시 무효화 실패: key={}, error={}", key, evictError.getMessage());
                }
                return null;
            } catch (ClassCastException e) {
                log.warn("캐시 역직렬화 오류 (타입 불일치), 캐시 무효화: key={}, type={}, error={}",
                        key, type.getName(), e.getMessage());
                try {
                    delegate.evict(key);
                } catch (Exception evictError) {
                    log.debug("캐시 무효화 실패: key={}, error={}", key, evictError.getMessage());
                }
                return null;
            } catch (Exception e) {
                log.debug("캐시 조회 중 오류 발생 (무시됨): key={}, error={}", key, e.getMessage());
                return null;
            }
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            try {
                return delegate.get(key, valueLoader);
            } catch (ClassCastException e) {
                log.warn("캐시 역직렬화 오류 (타입 불일치), 캐시 무효화 후 값 로더 실행: key={}, error={}", key, e.getMessage());
                try {
                    delegate.evict(key);
                } catch (Exception evictError) {
                    log.debug("캐시 무효화 실패: key={}, error={}", key, evictError.getMessage());
                }
                try {
                    return valueLoader.call();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } catch (Exception e) {
                log.debug("캐시 조회 중 오류 발생, 값 로더 실행: key={}, error={}", key, e.getMessage());
                try {
                    return valueLoader.call();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        @Override
        public void put(Object key, Object value) {
            try {
                delegate.put(key, value);
            } catch (Exception e) {
                log.debug("캐시 저장 중 오류 발생 (무시됨): key={}, error={}", key, e.getMessage());
            }
        }

        @Override
        public void evict(Object key) {
            try {
                delegate.evict(key);
            } catch (Exception e) {
                log.debug("캐시 제거 중 오류 발생 (무시됨): key={}, error={}", key, e.getMessage());
            }
        }

        @Override
        public void clear() {
            try {
                delegate.clear();
            } catch (Exception e) {
                log.debug("캐시 전체 제거 중 오류 발생 (무시됨): error={}", e.getMessage());
            }
        }
    }
}

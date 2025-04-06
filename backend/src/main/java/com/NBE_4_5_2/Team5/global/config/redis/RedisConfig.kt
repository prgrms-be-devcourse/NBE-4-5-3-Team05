package com.NBE_4_5_2.Team5.global.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.NBE_4_5_2.Team5.global.redis.RedisSubscriber;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@Profile("!test")
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfig {

	@Bean
	public RedisConnectionFactory redisConnectionFactory(Environment environment) {
		String redisHost = environment.getProperty("spring.data.redis.host");
		int redisPort = Integer.parseInt(environment.getProperty("spring.data.redis.port"));

		LettuceConnectionFactory factory = new LettuceConnectionFactory(redisHost, redisPort);
		factory.afterPropertiesSet();
		return factory;
	}

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    /**
     * 단일 Topic 사용을 위한 Bean 설정
     */
    @Bean
    public ChannelTopic channelTopic() {
        return new ChannelTopic("chatroom");
    }

	/**
	 * redis에 발행(publish)된 메시지 처리를 위한 리스너 설정
	 */
	@Bean
	public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory,
		MessageListenerAdapter listenerAdapter,
		ChannelTopic channelTopic) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, channelTopic);
		return container;
	}

	/**
	 * 실제 메시지를 처리하는 subscriber 설정 추가
	 */
	@Bean
	public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
		return new MessageListenerAdapter(subscriber, "sendMessage");
	}

	/**
	 * 어플리케이션에서 사용할 redisTemplate 설정
	 */
	@Bean
	public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		return redisTemplate;
	}

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
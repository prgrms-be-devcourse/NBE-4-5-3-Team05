package com.NBE_4_5_2.Team5.global.config.redis

import com.NBE_4_5_2.Team5.global.redis.RedisSubscriber
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisKeyValueAdapter
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@Profile("!test")
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
class RedisConfig {

    @Bean
    fun redisConnectionFactory(environment: Environment): RedisConnectionFactory {
        val redisHost = environment.getProperty("spring.data.redis.host")
        val redisPort = environment.getProperty("spring.data.redis.port")!!.toInt()

        val factory = LettuceConnectionFactory(redisHost!!, redisPort)
        factory.afterPropertiesSet()
        return factory
    }

    @Bean
    fun stringRedisTemplate(redisConnectionFactory: RedisConnectionFactory): StringRedisTemplate {
        return StringRedisTemplate(redisConnectionFactory)
    }

    /**
     * 단일 Topic 사용을 위한 Bean 설정
     */
    @Bean
    fun channelTopic(): ChannelTopic {
        return ChannelTopic("chatroom")
    }

    /**
     * redis에 발행(publish)된 메시지 처리를 위한 리스너 설정
     */
    @Bean
    fun redisMessageListener(
        connectionFactory: RedisConnectionFactory,
        listenerAdapter: MessageListenerAdapter,
        channelTopic: ChannelTopic
    ): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            addMessageListener(listenerAdapter, channelTopic)
        }
    }

    /**
     * 실제 메시지를 처리하는 subscriber 설정 추가
     */
    @Bean
    fun listenerAdapter(subscriber: RedisSubscriber): MessageListenerAdapter {
        return MessageListenerAdapter(subscriber, "sendMessage")
    }

    /**
     * 어플리케이션에서 사용할 redisTemplate 설정
     */
    @Bean
    fun objectRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = Jackson2JsonRedisSerializer(String::class.java)
        }
    }

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory)
    : RedisTemplate<String, String> {
       return RedisTemplate<String, String>().apply {
            connectionFactory = redisConnectionFactory

            val stringSerializer = StringRedisSerializer()
            keySerializer = stringSerializer
            valueSerializer = stringSerializer
            hashKeySerializer = stringSerializer
            hashValueSerializer = stringSerializer

            afterPropertiesSet()
        }
    }
}
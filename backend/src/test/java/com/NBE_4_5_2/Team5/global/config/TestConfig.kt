package com.NBE_4_5_2.Team5.global.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import org.testcontainers.utility.DockerImageName
import util.Util

@TestConfiguration
class TestConfig {

    @Bean
    fun util(): Util = Util()

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()

    companion object {
        private val redisTestContainer = GenericContainer(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379)
            .withReuse(true)

        init {
            redisTestContainer.start()
            System.setProperty("spring.data.redis.host", redisTestContainer.host)
            System.setProperty("spring.data.redis.port", redisTestContainer.getMappedPort(6379).toString())
        }
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    fun redisContainer(): GenericContainer<*> = redisTestContainer

    @Bean(destroyMethod = "")
    @Primary
    fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory(
            redisTestContainer.host,
            redisTestContainer.getMappedPort(6379)
        ).apply {
            setValidateConnection(true)
            afterPropertiesSet()
            start()
        }
    }

    @Bean
    fun stringRedisTemplate(redisConnectionFactory: RedisConnectionFactory): StringRedisTemplate =
        StringRedisTemplate(redisConnectionFactory)

    @Bean
    @Primary
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            setConnectionFactory(redisConnectionFactory)
            val stringSerializer = StringRedisSerializer()
            keySerializer = stringSerializer
            valueSerializer = stringSerializer
            hashKeySerializer = stringSerializer
            hashValueSerializer = stringSerializer
            afterPropertiesSet()
        }
    }

    @Bean
    fun objectRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = Jackson2JsonRedisSerializer(String::class.java)
        }
    }

    @Bean
    fun channelTopic(): ChannelTopic = ChannelTopic("test-chatroom")
}

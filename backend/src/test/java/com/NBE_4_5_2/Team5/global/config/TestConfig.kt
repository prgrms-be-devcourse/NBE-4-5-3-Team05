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

@TestConfiguration
class TestConfig {

    @Bean
    fun util(): Util = Util()

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()

    companion object {
        private val redisTestContainer = GenericContainer(
            DockerImageName.parse("redis:latest")
        )
            .withExposedPorts(6379)
            .withReuse(true)

        init {
            redisTestContainer.start()
            System.setProperty("spring.data.redis.host", redisTestContainer.host)
            System.setProperty("spring.data.redis.port", redisTestContainer.getMappedPort(6379).toString())
        }
    }

    @Bean
    fun redisContainer(): GenericContainer<*> = redisTestContainer

    @Bean
    @Primary
    fun redisConnectionFactory(): RedisConnectionFactory {
        val factory = LettuceConnectionFactory(redisTestContainer.host, redisTestContainer.getMappedPort(6379))
        factory.setValidateConnection(true)
        factory.afterPropertiesSet()
        return factory
    }

    @Bean
    fun stringRedisTemplate(redisConnectionFactory: RedisConnectionFactory): StringRedisTemplate =
        StringRedisTemplate(redisConnectionFactory)

    @Bean
    @Primary
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = redisConnectionFactory

        val stringSerializer = StringRedisSerializer()
        template.keySerializer = stringSerializer
        template.valueSerializer = stringSerializer
        template.hashKeySerializer = stringSerializer
        template.hashValueSerializer = stringSerializer

        template.afterPropertiesSet()
        return template
    }

    @Bean
    fun objectRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.setConnectionFactory(connectionFactory)
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = Jackson2JsonRedisSerializer(String::class.java)
        return redisTemplate
    }

    @Bean
    fun channelTopic(): ChannelTopic = ChannelTopic("test-chatroom")
}

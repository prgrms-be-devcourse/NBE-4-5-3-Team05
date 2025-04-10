package com.NBE_4_5_2.Team5.global.standard.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*

class Ut {
    object Json {
        private val objectMapper = ObjectMapper()

        fun toString(obj: Any): String {
            return try {
                objectMapper.writeValueAsString(obj)
            } catch (e: JsonProcessingException) {
                throw RuntimeException(e)
            }
        }
    }

    object Jwt {
        fun createToken(keyString: String, expireSeconds: Int, claims: Map<String, Any>): String {
            val secretKey = Keys.hmacShaKeyFor(keyString.toByteArray())

            val issuedAt = Date()
            val expiration = Date(issuedAt.time + 1000L * expireSeconds)

            return Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact()


        }

        fun isValidToken(keyString: String, token: String): Boolean {
            return try {
                val secretKey = Keys.hmacShaKeyFor(keyString.toByteArray())

                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(token)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }

        }

        fun getPayload(keyString: String, jwtStr: String): Map<String, Any> {
            val secretKey = Keys.hmacShaKeyFor(keyString.toByteArray())

            return Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwtStr)
                .payload
        }
    }

    object Notification {
        private val objectMapper = ObjectMapper()

        fun parse(message: String): com.NBE_4_5_2.Team5.domain.notification.entity.Notification {
            return objectMapper.readValue<com.NBE_4_5_2.Team5.domain.notification.entity.Notification>(message);
        }
    }
}
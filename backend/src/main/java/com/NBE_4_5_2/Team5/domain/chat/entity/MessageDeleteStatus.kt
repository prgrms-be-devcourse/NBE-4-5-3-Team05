package com.NBE_4_5_2.Team5.domain.chat.entity

import jakarta.persistence.*

@Entity
class MessageDeleteStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private val message: ChatMessage,

    @Column(nullable = false)
    private var username: String,

    @Column(nullable = false)
    private var isDeleted:Boolean = false
) {

    fun getDeleteStatusByUser(username: String?): Boolean {
        return this.isDeleted
    }
}
package com.NBE_4_5_2.Team5.domain.user.admin.entity

import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.querydsl.core.types.Projections.constructor
import jakarta.persistence.*
import lombok.Builder
import org.springframework.boot.actuate.autoconfigure.cloudfoundry.CloudFoundryAuthorizationException.Reason
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
class BanList(
    @Id
    private val _id: String = "ban-" + UUID.randomUUID(),
    @Column(name="reason")
    private var _reason: String,
    @OneToOne
    @JoinColumn(name = "user_id")
    private var _bannedUser: User,
    @CreatedDate
    private val _startDate: LocalDateTime=LocalDateTime.now(),

    @Column(name="endDate")
    private var _endDate: LocalDateTime,
) {
    val id:String
        get()=_id

    val reason:String
        get()=_reason

    val bannedUser:User
        get()=_bannedUser

    val startDate:LocalDateTime
        get()=_startDate

    val endDate:LocalDateTime
        get()=_endDate

    constructor(reason:String, bannedUser:User, endDate:LocalDateTime):this(
        _reason=reason,
        _bannedUser=bannedUser,
        _endDate=endDate
    )
}

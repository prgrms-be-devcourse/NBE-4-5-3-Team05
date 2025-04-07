package com.NBE_4_5_2.Team5.domain.user.admin.entity

import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import jakarta.persistence.*
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@NoArgsConstructor
@Getter
class BanList @Builder constructor(
    @Id
    private val _id: String = "ban-" + UUID.randomUUID(),
    @CreatedDate
    private val _startDate: LocalDateTime=LocalDateTime.now(),
    private var _reason: String,
    @JoinColumn(name = "user_id")
    @OneToOne
    private var _bannedUser: User,
    private var _endDate: LocalDateTime
) {
    val id:String
        get()=_id
    val startDate:LocalDateTime
        get()=_startDate
    val reason:String
        get()=_reason
    val bannedUser:User
        get()=_bannedUser
    val endDate:LocalDateTime
        get()=_endDate

    constructor(reason:String, bannedUser:User, endDate:LocalDateTime):this(
        _reason=reason,
        _bannedUser=bannedUser,
        _endDate=endDate
    )
}

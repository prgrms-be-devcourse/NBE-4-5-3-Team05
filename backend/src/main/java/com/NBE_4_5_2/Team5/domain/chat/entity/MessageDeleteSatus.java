package com.NBE_4_5_2.Team5.domain.chat.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MessageDeleteSatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "message_id",nullable = false)
    private ChatMessage message;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private boolean isDeleted=false;

    public boolean getDeleteStatusByUser(String username) {
        return this.isDeleted;
    }
}

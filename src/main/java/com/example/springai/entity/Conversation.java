/*
 * =============================================================================
 * 클래스명 : Conversation
 * =============================================================================
 * 목적
 *  - 사용자 질문과 AI 응답을 저장하는 Entity
 */

package com.example.springai.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* 대화를 생성한 사용자 아이디 */
    @Column(nullable = false)
    private Long userId;

    /* 로그인 사용자명 */
    @Column(nullable = false)
    private String username;

    /* 사용자 질문 */
    @Column(nullable = false, length = 4000)
    private String question;

    /* AI 응답 */
    @Column(nullable = false, length = 10000)
    private String answer;

    /* 대화 생성 시간 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Conversation() {
    }

    public Conversation(
            Long userId,
            String username,
            String question,
            String answer) {

        this.userId = userId;
        this.username = username;
        this.question = question;
        this.answer = answer;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
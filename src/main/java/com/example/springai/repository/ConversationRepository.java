/*
 * =============================================================================
 * 인터페이스명 : ConversationRepository
 * =============================================================================
 * 목적
 *  - 대화 데이터를 저장하고 조회하는 Repository
 */

package com.example.springai.repository;

import com.example.springai.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

    /* 특정 사용자의 대화 목록을 최신순으로 조회한다. */
    List<Conversation> findByUserIdOrderByCreatedAtDesc(Long userId);
}
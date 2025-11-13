package com.sleekydz86.payment2v2.domain.member.port.out;

import com.sleekydz86.payment2v2.domain.member.model.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findByEmail(String email);
    Optional<Member> findById(Long id);
    boolean existsByEmail(String email);
    Member save(Member member);
    List<Member> findAll();
    List<Member> findByNameContainingIgnoreCase(String name);
    List<Member> findByEmailContainingIgnoreCase(String email);
}


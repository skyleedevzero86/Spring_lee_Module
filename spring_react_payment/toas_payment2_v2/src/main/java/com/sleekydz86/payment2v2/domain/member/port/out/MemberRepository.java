package com.sleekydz86.payment2v2.domain.member.port.out;

import com.sleekydz86.payment2v2.domain.member.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findByEmail(String email);
    Optional<Member> findById(Long id);
    boolean existsByEmail(String email);
    Member save(Member member);
    List<Member> findAll();
    Page<Member> findAll(Pageable pageable);
    List<Member> findByNameContainingIgnoreCase(String name);
    Page<Member> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<Member> findByEmailContainingIgnoreCase(String email);
    Page<Member> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}


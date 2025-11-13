package com.sleekydz86.payment2v2.domain.member.adapter.out.persistence;

import com.sleekydz86.payment2v2.domain.member.model.Member;
import com.sleekydz86.payment2v2.domain.member.port.out.MemberRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberJpaRepository extends JpaRepository<Member, Long>, MemberRepository {
    @Override
    @Query("SELECT m FROM Member m WHERE m.email.value = :email")
    Optional<Member> findByEmail(@Param("email") String email);
    
    @Override
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m WHERE m.email.value = :email")
    boolean existsByEmail(@Param("email") String email);
    
    @Override
    @Query("SELECT m FROM Member m WHERE LOWER(m.name.value) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Member> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Override
    @Query("SELECT m FROM Member m WHERE LOWER(m.email.value) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<Member> findByEmailContainingIgnoreCase(@Param("email") String email);
}


package com.sleekydz86.loginstudy.member.repository;

import com.sleekydz86.loginstudy.member.domain.MemberProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MemberProfileRepository
		extends JpaRepository<MemberProfile, Long>, JpaSpecificationExecutor<MemberProfile> {

	@EntityGraph(attributePaths = {"address", "preferences"})
	Optional<MemberProfile> findOneById(Long id);

	@EntityGraph(attributePaths = {"address", "preferences"})
	Optional<MemberProfile> findOneByUserSubject(String userSubject);

	boolean existsByUserSubject(String userSubject);
}

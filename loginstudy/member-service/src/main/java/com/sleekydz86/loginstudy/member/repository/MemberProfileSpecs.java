package com.sleekydz86.loginstudy.member.repository;

import com.sleekydz86.loginstudy.member.domain.MemberProfile;
import com.sleekydz86.loginstudy.member.domain.MemberStatus;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class MemberProfileSpecs {

	private MemberProfileSpecs() {
	}

	public static Specification<MemberProfile> search(
			MemberStatus status,
			String email,
			String name,
			Instant joinedFrom,
			Instant joinedTo) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			}
			if (email != null && !email.isBlank()) {
				predicates.add(cb.equal(cb.lower(root.get("email")), email.toLowerCase()));
			}
			if (name != null && !name.isBlank()) {
				predicates.add(cb.like(cb.lower(root.get("displayName")), "%" + name.toLowerCase() + "%"));
			}
			if (joinedFrom != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("joinedAt"), joinedFrom));
			}
			if (joinedTo != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("joinedAt"), joinedTo));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}

	public static Specification<MemberProfile> keyset(
			MemberStatus status,
			Instant joinedFrom,
			Instant joinedTo,
			Instant cursorJoinedAt,
			Long cursorId) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			}
			if (joinedFrom != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("joinedAt"), joinedFrom));
			}
			if (joinedTo != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("joinedAt"), joinedTo));
			}
			if (cursorJoinedAt != null && cursorId != null) {
				predicates.add(cb.or(
						cb.lessThan(root.get("joinedAt"), cursorJoinedAt),
						cb.and(
								cb.equal(root.get("joinedAt"), cursorJoinedAt),
								cb.lessThan(root.get("id"), cursorId))));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}
}

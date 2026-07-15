package com.sleekydz86.loginstudy.auth.repository;

import com.sleekydz86.loginstudy.auth.domain.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	Optional<UserAccount> findByUsername(String username);

	boolean existsByUsername(String username);
}

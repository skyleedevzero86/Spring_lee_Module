package com.sleekydz86.loginstudy.auth.repository;

import com.sleekydz86.loginstudy.auth.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	Optional<UserAccount> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

	List<UserAccount> findAllByDisplayNameAndPhoneOrderByCreatedAtAsc(String displayName, String phone);

	Optional<UserAccount> findByUsernameAndEmailIgnoreCaseAndPhone(String username, String email, String phone);
}

package com.sleekydz86.loginstudy.auth.repository;

import com.sleekydz86.loginstudy.auth.domain.LoginHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

	List<LoginHistory> findTop50ByUsernameOrderByCreatedAtDesc(String username);

	long countByUsernameAndSuccess(String username, boolean success);
}

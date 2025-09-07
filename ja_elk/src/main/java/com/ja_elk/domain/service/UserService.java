package com.ja_elk.domain.service;

import com.ja_elk.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final Random random = new Random();

    @Autowired
    private FluentdLoggingService fluentdLoggingService;

    private final String[] names = {"김철수", "이영희", "박민수", "최지영", "정현우"};
    private final String[] departments = {"개발팀", "마케팅팀", "영업팀", "인사팀", "기획팀"};

    public List<User> generateDummyUsers(int count) {
        logger.info("더미 사용자 {}명 생성 시작", count);
        fluentdLoggingService.logInfo("더미 사용자 " + count + "명 생성 시작", this.getClass().getName());

        List<User> users = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String name = names[random.nextInt(names.length)];
            String department = departments[random.nextInt(departments.length)];
            String email = name.toLowerCase() + i + "@company.com";

            User user = new User((long) i, name, email, department);
            users.add(user);

            logger.debug("사용자 생성: {}", user);

            if (random.nextBoolean()) {
                logger.info("사용자 처리 완료: {} (부서: {})", name, department);
                fluentdLoggingService.logInfo(
                        "사용자 처리 완료: " + name + " (부서: " + department + ")",
                        this.getClass().getName()
                );
            }

            if (random.nextInt(10) == 0) {
                logger.warn("사용자 {}의 이메일 형식 검증 필요: {}", name, email);
                fluentdLoggingService.logWarn(
                        "사용자 " + name + "의 이메일 형식 검증 필요: " + email,
                        this.getClass().getName()
                );
            }

            if (random.nextInt(50) == 0) {
                logger.error("사용자 {} 처리 중 예외 발생 (시뮬레이션)", name);
                fluentdLoggingService.logError(
                        "사용자 " + name + " 처리 중 예외 발생 (시뮬레이션)",
                        this.getClass().getName(),
                        null
                );
            }
        }

        logger.info("더미 사용자 생성 완료: {}명", count);
        fluentdLoggingService.logInfo("더미 사용자 생성 완료: " + count + "명", this.getClass().getName());
        return users;
    }

    public User findById(Long id) {
        logger.debug("사용자 조회 요청: ID = {}", id);

        if (id <= 0) {
            logger.warn("잘못된 사용자 ID: {}", id);
            fluentdLoggingService.logWarn("잘못된 사용자 ID: " + id, this.getClass().getName());
            return null;
        }

        if (random.nextInt(20) == 0) {
            logger.error("데이터베이스 연결 오류 시뮬레이션 - 사용자 ID: {}", id);
            RuntimeException ex = new RuntimeException("DB 연결 실패");
            fluentdLoggingService.logError(
                    "데이터베이스 연결 오류 시뮬레이션 - 사용자 ID: " + id,
                    this.getClass().getName(),
                    ex
            );
            throw ex;
        }

        String name = names[random.nextInt(names.length)];
        String department = departments[random.nextInt(departments.length)];
        User user = new User(id, name, name.toLowerCase() + "@company.com", department);

        logger.info("사용자 조회 성공: {}", user);
        fluentdLoggingService.logInfo("사용자 조회 성공: " + user.toString(), this.getClass().getName());
        return user;
    }
}
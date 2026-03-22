package com.sleekydz86.oidstudy.oidc.domain.notification;

import java.time.LocalDateTime;
import com.sleekydz86.oidstudy.oidc.domain.user.UserAccount;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminNotification {

    private Long id;
    private String category;
    private String title;
    private String message;
    private Long targetUserId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static AdminNotification withdrawal(UserAccount account) {
        AdminNotification notification = new AdminNotification();
        notification.category = "USER_WITHDRAWAL";
        notification.title = "탈퇴 회원 발생";
        notification.message = String.format(
                "%s (%s) 계정이 탈퇴 처리되었습니다. 로그인 아이디: %s. 관리자 확인이 필요합니다.",
                fallback(account.getDisplayName(), account.getEmail(), account.getProviderUserId()),
                fallback(account.getEmail(), account.getProviderUserId()),
                fallback(account.getLoginId(), "미등록")
        );
        notification.targetUserId = account.getId();
        return notification;
    }

    private static String fallback(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "알 수 없음";
    }
}
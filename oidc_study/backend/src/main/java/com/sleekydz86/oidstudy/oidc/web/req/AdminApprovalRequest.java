package com.sleekydz86.oidstudy.oidc.web.req;

import java.util.List;

public record AdminApprovalRequest(List<String> roles) {
}
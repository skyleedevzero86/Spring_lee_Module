package com.sleekydz86.searchai.gateway.global.grpc;

import com.sleekydz86.searchai.gateway.global.security.AuthUser;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.springframework.grpc.client.GlobalClientInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@GlobalClientInterceptor
public final class GrpcClientAuthInterceptor implements ClientInterceptor {

	private static final Metadata.Key<String> META_USER_ID =
		Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);
	private static final Metadata.Key<String> META_USERNAME =
		Metadata.Key.of("x-username", Metadata.ASCII_STRING_MARSHALLER);
	private static final Metadata.Key<String> META_REQUEST_ID =
		Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);
	private static final Metadata.Key<String> META_TRACE_ID =
		Metadata.Key.of("x-trace-id", Metadata.ASCII_STRING_MARSHALLER);

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
		MethodDescriptor<ReqT, RespT> method,
		CallOptions callOptions,
		Channel next
	) {
		return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
			@Override
			public void start(Listener<RespT> responseListener, Metadata headers) {
				if (headers.get(META_REQUEST_ID) == null) {
					headers.put(META_REQUEST_ID, UUID.randomUUID().toString());
				}
				if (headers.get(META_TRACE_ID) == null) {
					headers.put(META_TRACE_ID, UUID.randomUUID().toString());
				}
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				if (authentication != null && authentication.getPrincipal() instanceof AuthUser user) {
					if (headers.get(META_USER_ID) == null) {
						headers.put(META_USER_ID, user.username());
					}
					if (headers.get(META_USERNAME) == null) {
						headers.put(META_USERNAME, user.username());
					}
				}
				super.start(responseListener, headers);
			}
		};
	}
}

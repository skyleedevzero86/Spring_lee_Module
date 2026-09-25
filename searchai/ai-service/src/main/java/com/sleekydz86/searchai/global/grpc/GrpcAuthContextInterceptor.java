package com.sleekydz86.searchai.global.grpc;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalServerInterceptor
public final class GrpcAuthContextInterceptor implements ServerInterceptor {

	public static final Context.Key<String> USER_ID = Context.key("userId");
	public static final Context.Key<String> USERNAME = Context.key("username");
	public static final Context.Key<String> REQUEST_ID = Context.key("requestId");
	public static final Context.Key<String> TRACE_ID = Context.key("traceId");

	private static final Metadata.Key<String> META_USER_ID =
		Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);
	private static final Metadata.Key<String> META_USERNAME =
		Metadata.Key.of("x-username", Metadata.ASCII_STRING_MARSHALLER);
	private static final Metadata.Key<String> META_REQUEST_ID =
		Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);
	private static final Metadata.Key<String> META_TRACE_ID =
		Metadata.Key.of("x-trace-id", Metadata.ASCII_STRING_MARSHALLER);

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
		ServerCall<ReqT, RespT> call,
		Metadata headers,
		ServerCallHandler<ReqT, RespT> next
	) {
		Context ctx = Context.current()
			.withValue(USER_ID, headers.get(META_USER_ID))
			.withValue(USERNAME, headers.get(META_USERNAME))
			.withValue(REQUEST_ID, headers.get(META_REQUEST_ID))
			.withValue(TRACE_ID, headers.get(META_TRACE_ID));
		return Contexts.interceptCall(ctx, call, headers, next);
	}
}

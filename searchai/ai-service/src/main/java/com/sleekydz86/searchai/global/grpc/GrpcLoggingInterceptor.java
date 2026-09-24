package com.sleekydz86.searchai.global.grpc;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalServerInterceptor
public final class GrpcLoggingInterceptor implements ServerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(GrpcLoggingInterceptor.class);

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
		ServerCall<ReqT, RespT> call,
		Metadata headers,
		ServerCallHandler<ReqT, RespT> next
	) {
		long started = System.nanoTime();
		String method = call.getMethodDescriptor().getFullMethodName();
		log.info("gRPC 호출 시작: {}", method);
		return new io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
			next.startCall(new io.grpc.ForwardingServerCall.SimpleForwardingServerCall<>(call) {
				@Override
				public void close(io.grpc.Status status, Metadata trailers) {
					long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
					log.info("gRPC 호출 종료: {} / 상태={} / 지연={}ms", method, status.getCode(), elapsedMs);
					super.close(status, trailers);
				}
			}, headers)
		) {
		};
	}
}

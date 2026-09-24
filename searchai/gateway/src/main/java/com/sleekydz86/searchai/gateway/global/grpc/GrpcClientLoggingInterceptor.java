package com.sleekydz86.searchai.gateway.global.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.client.GlobalClientInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalClientInterceptor
public final class GrpcClientLoggingInterceptor implements ClientInterceptor {

	private static final Logger log = LoggerFactory.getLogger(GrpcClientLoggingInterceptor.class);

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
		MethodDescriptor<ReqT, RespT> method,
		CallOptions callOptions,
		Channel next
	) {
		long started = System.nanoTime();
		String fullMethod = method.getFullMethodName();
		log.info("Gateway → gRPC 호출 시작: {}", fullMethod);
		return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
			@Override
			public void start(Listener<RespT> responseListener, io.grpc.Metadata headers) {
				super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
					@Override
					public void onClose(Status status, io.grpc.Metadata trailers) {
						long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
						log.info("Gateway → gRPC 호출 종료: {} / 상태={} / 지연={}ms", fullMethod, status.getCode(), elapsedMs);
						super.onClose(status, trailers);
					}
				}, headers);
			}
		};
	}
}

package com.sleekydz86.searchai.global.grpc;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@GlobalServerInterceptor
public final class GrpcMetricsInterceptor implements ServerInterceptor {

	private final MeterRegistry meterRegistry;

	public GrpcMetricsInterceptor(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
		ServerCall<ReqT, RespT> call,
		Metadata headers,
		ServerCallHandler<ReqT, RespT> next
	) {
		String method = call.getMethodDescriptor().getFullMethodName();
		long started = System.nanoTime();
		return new io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
			next.startCall(new io.grpc.ForwardingServerCall.SimpleForwardingServerCall<>(call) {
				@Override
				public void close(io.grpc.Status status, Metadata trailers) {
					long elapsed = System.nanoTime() - started;
					Timer.builder("grpc.request.duration")
						.tag("method", method)
						.tag("status", status.getCode().name())
						.register(meterRegistry)
						.record(elapsed, TimeUnit.NANOSECONDS);
					if (!status.isOk()) {
						meterRegistry.counter("grpc.request.failure", "method", method, "status", status.getCode().name())
							.increment();
					}
					super.close(status, trailers);
				}
			}, headers)
		) {
		};
	}
}

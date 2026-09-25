package com.sleekydz86.searchai.global.grpc;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalServerInterceptor
@ConditionalOnBean(ObservationRegistry.class)
public final class GrpcTracingInterceptor implements ServerInterceptor {

	private final ObservationRegistry observationRegistry;

	public GrpcTracingInterceptor(ObservationRegistry observationRegistry) {
		this.observationRegistry = observationRegistry;
	}

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
		ServerCall<ReqT, RespT> call,
		Metadata headers,
		ServerCallHandler<ReqT, RespT> next
	) {
		String method = call.getMethodDescriptor().getFullMethodName();
		Observation observation = Observation.createNotStarted("grpc.server", observationRegistry)
			.lowCardinalityKeyValue("rpc.method", method)
			.start();
		try {
			return new io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
				next.startCall(new io.grpc.ForwardingServerCall.SimpleForwardingServerCall<>(call) {
					@Override
					public void close(io.grpc.Status status, Metadata trailers) {
						observation.lowCardinalityKeyValue("rpc.status", status.getCode().name());
						observation.stop();
						super.close(status, trailers);
					}
				}, headers)
			) {
			};
		} catch (RuntimeException ex) {
			observation.error(ex);
			observation.stop();
			throw ex;
		}
	}
}

package com.sleekydz86.monitoring.logstack_s3.application.usecase;

@FunctionalInterface
public interface UseCase<I, O> {

    O apply(I input);
}

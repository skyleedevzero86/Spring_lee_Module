package com.sleekydz86.concurrentlab.global.util;

import java.util.concurrent.Callable;

import com.sleekydz86.concurrentlab.customer.domain.TimedResult;
import com.sleekydz86.concurrentlab.global.exception.LookupFailedException;

public final class LatencySimulator {

	private LatencySimulator() {
	}

	public static <T> TimedResult<T> call(long delayMillis, Callable<T> task) {
		Stopwatch stopwatch = Stopwatch.start();
		try {
			if (delayMillis > 0) {
				Thread.sleep(delayMillis);
			}
			return new TimedResult<>(task.call(), stopwatch.elapsedMillis());
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new LookupFailedException("지연 시뮬레이션이 중단되었습니다", ex);
		}
		catch (Exception ex) {
			throw new LookupFailedException("외부 API 호출에 실패했습니다", ex);
		}
	}
}

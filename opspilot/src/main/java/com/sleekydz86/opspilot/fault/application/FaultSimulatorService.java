package com.sleekydz86.opspilot.fault.application;

import com.sleekydz86.opspilot.fault.domain.SimulatedServerException;
import org.springframework.stereotype.Service;

@Service
public final class FaultSimulatorService {

	public void timeout(long delayMs) {
		long safe = Math.max(0L, Math.min(delayMs, 30_000L));
		try {
			Thread.sleep(safe);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new SimulatedServerException("SIMULATED_TIMEOUT_INTERRUPTED", "타임아웃 시뮬레이션이 중단되었습니다");
		}
	}

	public void http500() {
		throw new SimulatedServerException("SIMULATED_INTERNAL_ERROR", "의도적으로 발생시킨 서버 장애입니다.");
	}

	public void databaseSlow(long delayMs) {
		timeout(delayMs);
	}

	public void databaseError() {
		throw new SimulatedServerException("SIMULATED_DATABASE_ERROR", "의도적으로 발생시킨 데이터베이스 장애입니다.");
	}

	public void redisError() {
		throw new SimulatedServerException("SIMULATED_REDIS_ERROR", "의도적으로 발생시킨 Redis 장애입니다.");
	}
}

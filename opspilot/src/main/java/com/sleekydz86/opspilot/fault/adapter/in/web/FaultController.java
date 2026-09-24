package com.sleekydz86.opspilot.fault.adapter.in.web;

import com.sleekydz86.opspilot.fault.application.FaultSimulatorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/faults")
public final class FaultController {

	private final FaultSimulatorService faultSimulatorService;

	public FaultController(FaultSimulatorService faultSimulatorService) {
		this.faultSimulatorService = faultSimulatorService;
	}

	@PostMapping("/timeout")
	public Map<String, Object> timeout(@RequestParam(defaultValue = "5000") long delay) {
		faultSimulatorService.timeout(delay);
		return Map.of("status", "ok", "delayMs", delay);
	}

	@PostMapping("/http-500")
	public void http500() {
		faultSimulatorService.http500();
	}

	@PostMapping("/http-503")
	public ResponseEntity<Map<String, String>> http503() {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
			.body(Map.of("code", "SIMULATED_SERVICE_UNAVAILABLE", "message", "의도적으로 발생시킨 503 장애입니다."));
	}

	@PostMapping("/rate-limit")
	public ResponseEntity<Map<String, String>> rateLimit() {
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
			.body(Map.of("code", "SIMULATED_RATE_LIMIT", "message", "의도적으로 발생시킨 429 Rate Limit입니다."));
	}

	@PostMapping("/random")
	public ResponseEntity<?> random() {
		int pick = ThreadLocalRandom.current().nextInt(4);
		return switch (pick) {
			case 0 -> {
				faultSimulatorService.timeout(1000);
				yield ResponseEntity.ok(Map.of("status", "timeout-simulated"));
			}
			case 1 -> {
				faultSimulatorService.http500();
				yield ResponseEntity.ok().build();
			}
			case 2 -> http503();
			default -> rateLimit();
		};
	}

	@PostMapping("/database/slow")
	public Map<String, Object> databaseSlow(@RequestParam(defaultValue = "3000") long delay) {
		faultSimulatorService.databaseSlow(delay);
		return Map.of("status", "ok", "delayMs", delay);
	}

	@PostMapping("/database/error")
	public void databaseError() {
		faultSimulatorService.databaseError();
	}

	@PostMapping("/redis/error")
	public void redisError() {
		faultSimulatorService.redisError();
	}
}

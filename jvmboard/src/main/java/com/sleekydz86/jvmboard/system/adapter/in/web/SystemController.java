package com.sleekydz86.jvmboard.system.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sleekydz86.jvmboard.system.application.port.in.GetSystemSnapshotUseCase;

@RestController
@RequestMapping("/api/system")
public class SystemController {

	private final GetSystemSnapshotUseCase getSystemSnapshotUseCase;

	public SystemController(GetSystemSnapshotUseCase getSystemSnapshotUseCase) {
		this.getSystemSnapshotUseCase = getSystemSnapshotUseCase;
	}

	@GetMapping
	public SystemResponse get() {
		return SystemResponse.from(getSystemSnapshotUseCase.get());
	}
}

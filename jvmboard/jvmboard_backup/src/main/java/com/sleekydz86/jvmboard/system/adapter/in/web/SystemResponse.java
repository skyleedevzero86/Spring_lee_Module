package com.sleekydz86.jvmboard.system.adapter.in.web;

import com.sleekydz86.jvmboard.global.util.MemoryFormatter;
import com.sleekydz86.jvmboard.global.util.ProcessorFormatter;
import com.sleekydz86.jvmboard.global.util.UptimeFormatter;
import com.sleekydz86.jvmboard.system.domain.SystemSnapshot;

public record SystemResponse(
	String javaVersion,
	String jvm,
	String cpu,
	String heapUsed,
	String heapMax,
	String garbageCollector,
	String uptime,
	long heapUsedBytes,
	long heapMaxBytes
) {

	public static SystemResponse from(SystemSnapshot snapshot) {
		return new SystemResponse(
			Integer.toString(snapshot.javaFeatureVersion()),
			snapshot.jvmName(),
			ProcessorFormatter.label(snapshot.availableProcessors()),
			MemoryFormatter.megabytes(snapshot.heapUsedBytes()),
			MemoryFormatter.megabytes(snapshot.heapMaxBytes()),
			snapshot.garbageCollector(),
			UptimeFormatter.format(snapshot.uptime()),
			snapshot.heapUsedBytes(),
			snapshot.heapMaxBytes()
		);
	}
}

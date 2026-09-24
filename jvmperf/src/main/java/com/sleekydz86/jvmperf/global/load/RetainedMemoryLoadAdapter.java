package com.sleekydz86.jvmperf.global.load;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sleekydz86.jvmperf.global.exception.MemoryLoadException;
import com.sleekydz86.jvmperf.monitor.application.port.out.MemoryLoadPort;

@Component
public class RetainedMemoryLoadAdapter implements MemoryLoadPort {

	private final List<byte[]> retained = new ArrayList<>();

	@Override
	public synchronized int allocateMegabytes(int megabytes) {
		try {
			byte[] chunk = new byte[Math.multiplyExact(megabytes, 1024 * 1024)];
			retained.add(chunk);
			return megabytes;
		}
		catch (ArithmeticException | OutOfMemoryError ex) {
			throw new MemoryLoadException("메모리 부하 생성에 실패했습니다", ex);
		}
	}

	@Override
	public synchronized int retainedChunkCount() {
		return retained.size();
	}
}

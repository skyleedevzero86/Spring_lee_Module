package com.sleekydz86.catalogflow.application.port.out;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public interface CacheWarmupPort {

	int warmUp(int limit);

	class RecordingCacheWarmupPort implements CacheWarmupPort {

		private final List<Integer> calls = new CopyOnWriteArrayList<>();

		@Override
		public int warmUp(int limit) {
			calls.add(limit);
			return Math.max(limit, 0);
		}

		public List<Integer> calls() {
			return List.copyOf(calls);
		}
	}
}

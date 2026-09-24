package com.sleekydz86.jvmperf.monitor.adapter.in.web;

import com.sleekydz86.jvmperf.global.util.MemoryFormatter;
import com.sleekydz86.jvmperf.monitor.domain.GcMetric;
import com.sleekydz86.jvmperf.monitor.domain.HeapMetric;
import com.sleekydz86.jvmperf.monitor.domain.JvmSnapshot;
import com.sleekydz86.jvmperf.monitor.domain.MemoryLoadResult;
import com.sleekydz86.jvmperf.monitor.domain.ThreadMetric;

public final class MonitorWebModels {

	private MonitorWebModels() {
	}

	public record HeapView(
		long usedBytes,
		long maxBytes,
		String usedLabel,
		String maxLabel,
		int usagePercent
	) {

		static HeapView from(HeapMetric heap) {
			return new HeapView(
				heap.usedBytes(),
				heap.maxBytes(),
				MemoryFormatter.megabytes(heap.usedBytes()),
				MemoryFormatter.megabytes(heap.maxBytes()),
				heap.usagePercent()
			);
		}
	}

	public record GcView(String name, long count, long timeMillis) {

		static GcView from(GcMetric gc) {
			return new GcView(gc.name(), gc.collectionCount(), gc.collectionTimeMillis());
		}
	}

	public record ThreadView(int platform, String virtualLabel) {

		static ThreadView from(ThreadMetric threads) {
			String virtual = threads.virtualThreads() == 0 ? "-" : Long.toString(threads.virtualThreads());
			return new ThreadView(threads.platformThreads(), virtual);
		}
	}

	public record SnapshotResponse(HeapView heap, GcView gc, ThreadView threads) {

		public static SnapshotResponse from(JvmSnapshot snapshot) {
			return new SnapshotResponse(
				HeapView.from(snapshot.heap()),
				GcView.from(snapshot.gc()),
				ThreadView.from(snapshot.threads())
			);
		}
	}

	public record LoadCompareView(String heapLabel, long gcCount) {

		static LoadCompareView from(JvmSnapshot snapshot) {
			return new LoadCompareView(
				MemoryFormatter.megabytes(snapshot.heap().usedBytes()),
				snapshot.gc().collectionCount()
			);
		}
	}

	public record MemoryLoadResponse(
		LoadCompareView before,
		LoadCompareView after,
		int allocatedMegabytes,
		int retainedChunks,
		SnapshotResponse snapshot
	) {

		public static MemoryLoadResponse from(MemoryLoadResult result) {
			return new MemoryLoadResponse(
				LoadCompareView.from(result.before()),
				LoadCompareView.from(result.after()),
				result.allocatedMegabytes(),
				result.retainedChunks(),
				SnapshotResponse.from(result.after())
			);
		}
	}
}

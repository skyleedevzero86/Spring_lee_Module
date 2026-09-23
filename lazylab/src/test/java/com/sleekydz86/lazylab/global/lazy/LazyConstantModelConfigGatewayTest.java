package com.sleekydz86.lazylab.global.lazy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.sleekydz86.lazylab.modelconfig.domain.ConfigAccessResult;
import com.sleekydz86.lazylab.modelconfig.domain.InitStatus;
import com.sleekydz86.lazylab.modelconfig.domain.ModelConfig;

class LazyConstantModelConfigGatewayTest {

	@Test
	void loadsOnceAndReturnsCachedValueQuickly() {
		AtomicInteger loads = new AtomicInteger();
		LazyConstantModelConfigGateway gateway = new LazyConstantModelConfigGateway(() -> {
			loads.incrementAndGet();
			try {
				Thread.sleep(120);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("로드가 중단되었습니다", ex);
			}
			return new ModelConfig("demo", "local", 0.1, 1024, "https://local/v1");
		});

		assertThat(gateway.status()).isEqualTo(InitStatus.NOT_INITIALIZED);

		ConfigAccessResult first = gateway.access();
		assertThat(first.firstAccess()).isTrue();
		assertThat(first.status()).isEqualTo(InitStatus.INITIALIZED);
		assertThat(first.loadTimeMillis()).isGreaterThanOrEqualTo(100);
		assertThat(loads.get()).isEqualTo(1);

		ConfigAccessResult second = gateway.access();
		assertThat(second.firstAccess()).isFalse();
		assertThat(second.loadTimeMillis()).isLessThan(40);
		assertThat(loads.get()).isEqualTo(1);
		assertThat(second.config()).isEqualTo(first.config());
	}
}

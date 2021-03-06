package io.lazyfox.cashiddemo;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class ExpiringBadgerNonceMap {

	private static final int EXPIRE_TIME = 10;

	private static final TimeUnit EXPIRE_TIME_UNIT = TimeUnit.MINUTES;

	private final ConcurrentMap<String, String> cacheAsMap;

	public ExpiringBadgerNonceMap() {
		Cache<String, String> cache = CacheBuilder.newBuilder().concurrencyLevel(4)
				.expireAfterWrite(EXPIRE_TIME, EXPIRE_TIME_UNIT).build();
		cacheAsMap = cache.asMap();
	}

	public void put(String key, String value) {
		cacheAsMap.put(key, value);
	}

	public String get(String key) {
		return cacheAsMap.get(key);
	}

	public void remove(String key) {
		cacheAsMap.remove(key);
	}
}

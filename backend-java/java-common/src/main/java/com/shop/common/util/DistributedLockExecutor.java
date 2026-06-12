package com.shop.common.util;

import com.shop.common.error.ErrorCode;
import com.shop.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockExecutor {

    private final RedissonClient redissonClient;

    public <T> T execute(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new ApiException(ErrorCode.SERVER_ERROR,
                        "요청이 많아 처리에 실패했습니다. 잠시 후 다시 시도해주세요. (잠금 획득 실패: " + lockKey + ")");
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.SERVER_ERROR, "시스템 오류가 발생했습니다. (스레드 중단됨: " + e + ")");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public <T> T executeMulti(List<String> lockKeys, long waitTime, long leaseTime, Supplier<T> supplier) {
        List<String> sortedKeys = new ArrayList<>(lockKeys);
        sortedKeys.sort(String::compareTo);

        List<RLock> locks = new ArrayList<>();
        try {
            for (String key : sortedKeys) {
                RLock lock = redissonClient.getLock(key);
                boolean isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
                if (!isLocked) {
                    throw new ApiException(ErrorCode.SERVER_ERROR,
                            "재고 확인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요. (잠금 획득 실패: " + key + ")");
                }
                locks.add(lock);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.SERVER_ERROR, "시스템 오류가 발생했습니다. (스레드 중단됨: " + e + ")");
        } finally {
            for (int i = locks.size() - 1; i >= 0; i--) {
                RLock lock = locks.get(i);
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}

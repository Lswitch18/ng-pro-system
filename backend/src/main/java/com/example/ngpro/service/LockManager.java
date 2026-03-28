package com.example.ngpro.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class LockManager {
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public boolean acquireLock(String resourceId, String owner) {
        ReentrantLock lock = locks.computeIfAbsent(resourceId, k -> new ReentrantLock());
        if (lock.tryLock()) {
            log.info("Lock ACQUIRED for resource [{}] by owner [{}]", resourceId, owner);
            return true;
        }
        log.warn("Lock DENIED for resource [{}] - already held by another process", resourceId);
        return false;
    }

    public void releaseLock(String resourceId, String owner) {
        ReentrantLock lock = locks.get(resourceId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("Lock RELEASED for resource [{}] by owner [{}]", resourceId, owner);
        }
    }

    public int getActiveLockCount() {
        return (int) locks.values().stream().filter(ReentrantLock::isLocked).count();
    }
}

package com.shop.common.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.Supplier;

@Component
public class TransactionHelper {

    @Transactional
    public <T> T execute(Supplier<T> supplier) {
        return supplier.get();
    }

    @Transactional
    public void execute(Runnable runnable) {
        runnable.run();
    }

    /**
     * Executes the given task after the current transaction successfully commits.
     * If no actual transaction is active, executes the task immediately.
     */
    public void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
}

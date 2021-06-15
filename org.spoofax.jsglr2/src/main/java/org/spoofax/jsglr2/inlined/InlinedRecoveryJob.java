package org.spoofax.jsglr2.inlined;

import java.util.HashMap;

public class InlinedRecoveryJob {
    int offset;
    int iteration;
    int iterationsQuota;
    final long timeoutAt;
    HashMap<InlinedStackNode, Integer> quota = new HashMap<>();
    HashMap<InlinedStackNode, Integer> lastRecoveredOffset = new HashMap<>();

    public InlinedRecoveryJob(int offset, int iterationsQuota, int timeout) {
        this.offset = offset;
        this.iteration = -1;
        this.iterationsQuota = iterationsQuota;
        this.timeoutAt = System.currentTimeMillis() + timeout;
    }

    boolean hasNextIteration() {
        return iteration + 1 < iterationsQuota;
    }

    int nextIteration() {
        return ++iteration;
    }

    void initQuota(InlinedActiveStacks activeStacks) {
        int quotaPerStack = iteration + 1;

        quota.clear();
        activeStacks.activeStacks.forEach(stack -> quota.put(stack, quotaPerStack));
    }

    int getQuota(InlinedStackNode stack) {
        return quota.getOrDefault(stack, 0);
    }

    int lastRecoveredOffset(InlinedStackNode stack) {
        return lastRecoveredOffset.getOrDefault(stack, -1);
    }

    void updateQuota(InlinedStackNode stack, int newQuota) {
        quota.merge(stack, newQuota, Math::max);
    }

    void updateLastRecoveredOffset(InlinedStackNode stack, int offset) {
        if (offset != -1)
            lastRecoveredOffset.merge(stack, offset, Math::max);
    }

    boolean timeout() {
        return System.currentTimeMillis() >= timeoutAt;
    }

}

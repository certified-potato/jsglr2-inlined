package org.spoofax.jsglr2.inlined.components;

import java.util.HashMap;
import java.util.Map;

import org.spoofax.jsglr2.inlined.components.observables.InlinedStackNode;

public class InlinedRecoveryJob {
    public int offset;
    public int iteration;
    final int iterationsQuota;
    final long timeoutAt;
    public HashMap<InlinedStackNode, Integer> quota = new HashMap<>();
    public HashMap<InlinedStackNode, Integer> lastRecoveredOffset = new HashMap<>();

    public InlinedRecoveryJob(int offset, int iterationsQuota, int timeout) {
        this.offset = offset;
        this.iteration = -1;
        this.iterationsQuota = iterationsQuota;
        this.timeoutAt = System.currentTimeMillis() + timeout;
    }

    public boolean hasNextIteration() {
        return iteration + 1 < iterationsQuota;
    }

    public int nextIteration() {
        return ++iteration;
    }

    public void initQuota(InlinedActiveStacks activeStacks) {
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

    public boolean timeout() {
        return System.currentTimeMillis() >= timeoutAt;
    }

}

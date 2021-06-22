package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.List;

public class InlinedObserving {
    private final List<IInlinedObserver> observers = new ArrayList<>();

    public void notify(IIinlinedNotification notification) {
        if(observers.isEmpty())
            return;

        for(IInlinedObserver observer : observers)
            notification.notify(observer);
    }

    public void attachObserver(IInlinedObserver observer) {
        observers.add(observer);
    }
    
    
    static interface IIinlinedNotification {
        void notify(IInlinedObserver observer);
    }
}

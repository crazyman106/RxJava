package atomic_demo;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCount extends AtomicInteger {

    public void add() {
        getAndIncrement();
    }
}

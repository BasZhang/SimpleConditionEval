package zorg.simpleCondition;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class SimpleCondition implements ICondition {

    private boolean val;
    private AtomicBoolean isLazy = new AtomicBoolean();
    private Function<Void, Boolean> lazyFunction;

    @Override
    public boolean get() {
        if (isLazy.get()) synchronized (this) {
            this.val = lazyFunction.apply(null);
            this.isLazy.set(false);
        }
        return val;
    }

    public static ICondition newEarge(boolean value) {
        SimpleCondition e = new SimpleCondition();
        e.isLazy.set(false);
        e.val = value;
        return e;
    }

    public static ICondition newLazy(Function<Void, Boolean> callFunction) {
        Objects.requireNonNull(callFunction);
        SimpleCondition l = new SimpleCondition();
        l.isLazy.set(true);
        l.lazyFunction = callFunction;
        return l;
    }
}
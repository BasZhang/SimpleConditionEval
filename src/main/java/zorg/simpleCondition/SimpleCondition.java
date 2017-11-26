package zorg.simpleCondition;

import java.util.Objects;
import java.util.function.Function;

public class SimpleCondition implements ICondition {

    private boolean val;
    private Boolean isLazy;
    private Function<Void, Boolean> lazyFunction;

    @Override
    public boolean get() {
        if (isLazy) synchronized (isLazy) {
            Boolean ans = lazyFunction.apply(null);
            this.val = ans;
            this.isLazy = false;
        }
        return val;
    }

    public static ICondition newEarge(boolean value) {
        SimpleCondition e = new SimpleCondition();
        e.isLazy = false;
        e.val = value;
        return e;
    }

    public static ICondition newLazy(Function<Void, Boolean> callFunction) {
        Objects.requireNonNull(callFunction);
        SimpleCondition l = new SimpleCondition();
        l.isLazy = true;
        l.lazyFunction = callFunction;
        return l;
    }
}

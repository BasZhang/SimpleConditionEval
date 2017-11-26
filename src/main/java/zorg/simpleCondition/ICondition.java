package zorg.simpleCondition;

public interface ICondition {
    boolean get();

    ICondition TRUE = () -> true;
    ICondition FALSE = () -> false;
}

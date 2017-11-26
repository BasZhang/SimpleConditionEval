package zorg.simpleCondition;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ExpressionTest {

    @Test
    public void basicTest() {
        Expression expression = new Expression("@1 & @b ? @c : @d",
                Map.of("1", ICondition.TRUE, "b", ICondition.FALSE, "c", ICondition.TRUE, "d", ICondition.FALSE));
        assertFalse(expression.eval());
    }
}
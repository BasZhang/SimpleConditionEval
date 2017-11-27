package zorg.simpleCondition;

import static org.junit.jupiter.api.Assertions

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;

public class ExpressionTest {

    @SuppressWarnings("serial")
    private Map<String, ICondition> context = new HashMap<String, ICondition>() {
        {
            put("1", ICondition.TRUE);
            put("b", ICondition.FALSE);
            put("b___1212", ICondition.FALSE);
            put("o1", ICondition.TRUE);
            put("o2", ICondition.FALSE);
            put("o3", ICondition.TRUE);
            put("o4", ICondition.FALSE);
        }
    };

    private List<Token> accessTokens(Expression e1) throws IllegalAccessException {
        Field f = FieldUtils.getField(Expression.class, "tokens", true);
        @SuppressWarnings("unchecked")
        List<Token> tokens = (List<Token>) f.get(e1);
        return tokens;
    }

    @Test
    public void testRef() throws Exception {
        Expression e1 = new Expression("@1", context);
        List<Token> tokens = accessTokens(e1);
        assertTrue(tokens.size() == 2);
        assertTrue(tokens.get(0) == Tokens.REF);
        assertTrue(tokens.get(1) instanceof Identifier);

        assertTrue(e1.eval());
    }

    @Test
    public void testAnd() throws Exception {
        Expression e1 = new Expression("@1 & @b", context);
        List<Token> tokens = accessTokens(e1);
        assertTrue(tokens.size() == 5);
        assertTrue(tokens.get(0) == Tokens.REF);
        assertTrue(tokens.get(1) instanceof Identifier);
        assertTrue(tokens.get(2) == Tokens.AND);
        assertTrue(tokens.get(3) == Tokens.REF);
        assertTrue(tokens.get(4) instanceof Identifier);

        assertFalse(e1.eval());
    }

    @Test
    public void testParen() throws Exception {
        Expression e1 = new Expression("(@1) & @b & @b___1212", context);
        List<Token> tokens = accessTokens(e1);
        assertTrue(tokens.size() == 10);
        assertTrue(tokens.get(0) == Tokens.OPEN_PAREN);
        assertTrue(tokens.get(1) == Tokens.REF);
        assertTrue(tokens.get(2) instanceof Identifier);
        assertTrue(tokens.get(3) == Tokens.CLOSE_PAREN);
        assertTrue(tokens.get(4) == Tokens.AND);
        assertTrue(tokens.get(5) == Tokens.REF);
        assertTrue(tokens.get(6) instanceof Identifier);
        assertTrue(tokens.get(7) == Tokens.AND);
        assertTrue(tokens.get(8) == Tokens.REF);
        assertTrue(tokens.get(9) instanceof Identifier);

        assertFalse(e1.eval());
    }

    @Test
    public void testSurroundingParen() throws Exception {
        Expression e1 = new Expression("(@1) & (@b & @b___1212)", context);
        List<Token> tokens = accessTokens(e1);
        assertTrue(tokens.size() == 12);
        assertTrue(tokens.get(0) == Tokens.OPEN_PAREN);
        assertTrue(tokens.get(1) == Tokens.REF);
        assertTrue(tokens.get(2) instanceof Identifier);
        assertTrue(tokens.get(3) == Tokens.CLOSE_PAREN);
        assertTrue(tokens.get(4) == Tokens.AND);
        assertTrue(tokens.get(5) == Tokens.OPEN_PAREN);
        assertTrue(tokens.get(6) == Tokens.REF);
        assertTrue(tokens.get(7) instanceof Identifier);
        assertTrue(tokens.get(8) == Tokens.AND);
        assertTrue(tokens.get(9) == Tokens.REF);
        assertTrue(tokens.get(10) instanceof Identifier);
        assertTrue(tokens.get(11) == Tokens.CLOSE_PAREN);

        assertFalse(e1.eval());
    }

    @Test
    public void testOr1() throws Exception {
        Expression e1 = new Expression("(@o1) & (@o2 | @o3)", context);
        List<Token> tokens = accessTokens(e1);
        assertTrue(tokens.size() == 12);
        assertTrue(tokens.get(0) == Tokens.OPEN_PAREN);
        assertTrue(tokens.get(1) == Tokens.REF);
        assertTrue(tokens.get(2) instanceof Identifier);
        assertTrue(tokens.get(3) == Tokens.CLOSE_PAREN);
        assertTrue(tokens.get(4) == Tokens.AND);
        assertTrue(tokens.get(5) == Tokens.OPEN_PAREN);
        assertTrue(tokens.get(6) == Tokens.REF);
        assertTrue(tokens.get(7) instanceof Identifier);
        assertTrue(tokens.get(8) == Tokens.OR);
        assertTrue(tokens.get(9) == Tokens.REF);
        assertTrue(tokens.get(10) instanceof Identifier);
        assertTrue(tokens.get(11) == Tokens.CLOSE_PAREN);

        assertTrue(e1.eval());
    }

    @Test
    public void testOr2() throws Exception {
        Expression e1 = new Expression("(@o2 | @o3 & @o4)", context);
        assertFalse(e1.eval());
        Expression e2 = new Expression("((@o2 | @o3) & @o1)", context);
        assertTrue(e2.eval());
    }

    @Test
    public void testNot() throws Exception {
        Expression e1 = new Expression("!(@o2 | @o3 & @o4)", context);
        assertTrue(e1.eval());
        Expression e2 = new Expression("((!@o2 | @o3) & @o1)", context);
        assertTrue(e2.eval());
    }

    @Test
    public void testConditional1() throws Exception {
        Expression e1 = new Expression("@1 ? !(@o2 | @o3 & @o4) : ((!@o2 | @o3) & @o1)", context);
        assertTrue(e1.eval());
    }

    @Test
    public void testConditional2() throws Exception {
        Expression e1 = new Expression("@1 ? (!@o2 ?@o3 | @o4 : @b) : ((!@o2 | @o3) & @o1)", context);
        assertTrue(e1.eval());
    }

    @Test
    public void testConditional3() throws Exception {
        Expression e1 = new Expression("@1 ? !(!@o2 ?@o3 & @o4 : @b) : ((!@o2 | @o3) & @o1)", context);
        assertTrue(e1.eval());
    }

    @Test
    public void testLazy1() throws Exception {
        final AtomicBoolean f11Got = new AtomicBoolean(false);
        this.context.put("f11", new ICondition() {

            @Override
            public boolean get() {
                f11Got.set(true);
                return false;
            }
        });
        final AtomicBoolean f12Got = new AtomicBoolean(false);
        this.context.put("f12", new ICondition() {

            @Override
            public boolean get() {
                f12Got.set(true);
                return false;
            }
        });
        final AtomicBoolean f13Got = new AtomicBoolean(false);
        this.context.put("f13", new ICondition() {

            @Override
            public boolean get() {
                f13Got.set(true);
                return true;
            }
        });
        Expression e1 = new Expression("@f11 ? @f12 : @f13", context);
        assertTrue(e1.eval());
        assertTrue(f11Got.get());
        assertFalse(f12Got.get());
        assertTrue(f13Got.get());
    }

    @Test
    public void testLazy2() throws Exception {
        final AtomicBoolean f21Got = new AtomicBoolean(false);
        this.context.put("f21", new ICondition() {

            @Override
            public boolean get() {
                f21Got.set(true);
                return true;
            }
        });
        final AtomicBoolean f22Got = new AtomicBoolean(false);
        this.context.put("f22", new ICondition() {

            @Override
            public boolean get() {
                f22Got.set(true);
                return true;
            }
        });
        final AtomicBoolean f23Got = new AtomicBoolean(false);
        this.context.put("f23", new ICondition() {

            @Override
            public boolean get() {
                f23Got.set(true);
                return true;
            }
        });
        Expression e1 = new Expression("@f21 & @f22 | @f23", context);
        assertTrue(e1.eval());
        assertTrue(f21Got.get());
        assertTrue(f22Got.get());
        assertFalse(f23Got.get());
    }

    @Test
    public void testLazy3() throws Exception {
        final AtomicBoolean f31Got = new AtomicBoolean(false);
        this.context.put("f31", new ICondition() {

            @Override
            public boolean get() {
                f31Got.set(true);
                return false;
            }
        });
        final AtomicBoolean f32Got = new AtomicBoolean(false);
        this.context.put("f32", new ICondition() {

            @Override
            public boolean get() {
                f32Got.set(true);
                return false;
            }
        });
        final AtomicBoolean f33Got = new AtomicBoolean(false);
        this.context.put("f33", new ICondition() {

            @Override
            public boolean get() {
                f33Got.set(true);
                return true;
            }
        });
        Expression e1 = new Expression("@f31 | @f33 | @f32", context);
        assertTrue(e1.eval());
        assertTrue(f31Got.get());
        assertFalse(f32Got.get());
        assertTrue(f33Got.get());
    }

    @Test(expected = ExpressionException.class)
    public void testException1() {
        Expression e1 = new Expression("@f31 | @f33 | @f32?", context);
        e1.eval();
    }

    @Test(expected = ExpressionException.class)
    public void testException2() {
        Expression e1 = new Expression("@1 | @b?", context);
        e1.eval();
    }
}

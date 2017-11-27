package zorg.simpleCondition;

public class Tokens {

    public static final Token REF = new Operator("@", 1);
    public static final Token NOT = new Operator("!", 2);
    public static final Token AND = new Operator("&", 3);
    public static final Token OR = new Operator("|", 4);
    public static final Token CONDITIONAL = new Operator("?", 5);
    public static final Token OPTION = new Operator(":", 5);

    public static final Token OPEN_PAREN = new Paren("(");
    public static final Token CLOSE_PAREN = new Paren(")");

    public static Token IDENTIFIER(String identifier) {
        return new Identifier(identifier);
    }

    public static Token VALUE(boolean v) {
        Identifier val = new Identifier(Boolean.toString(v));
        val.ref = v ? ICondition.TRUE : ICondition.FALSE;
        return val;
    }

    public static Token BINDED_REF(ICondition lazyCondition) {
        Identifier ref = new Identifier(null);
        ref.ref = lazyCondition;
        return ref;
    }

}

abstract class Token {

}

class Operator extends Token {
    final String op;
    final int precedence;

    Operator(String op, int precedence) {
        this.op = op;
        this.precedence = precedence;
    }

    @Override
    public String toString() {
        return "Operator{" + "op='" + op + '\'' + '}';
    }
}

class Identifier extends Token {
    final String identifier;
    ICondition ref;

    Identifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return "Identifier{" + "identifier='" + identifier + '\'' + ", ref=" + ((ref == null) ? null : "ref" + ref.hashCode()) + '}';
    }

}

class Paren extends Token {
    final String op;

    Paren(String op) {
        super();
        this.op = op;
    }

    @Override
    public String toString() {
        return "Paren{op='" + op + "'}";
    }

}
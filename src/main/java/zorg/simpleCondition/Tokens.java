package zorg.simpleCondition;

public class Tokens {

    public static final Token AND = new Operator("&", 4);

    public static final Token OR  = new Operator("|", 5);

    public static final Token NOT = new Operator("！", 3);

    public static final Token CONDITIONAL = new Operator("?", 6);

    public static final Token OPTION = new Operator(":", 7);

    public static final Token OPEN_PAREN = new Operator("(", 2);

    public static final Token CLOSE_PAREN = new Operator(")", 2);

    public static final Token REF = new Operator("@", 1);

    public static Token IDENTIFIER(String identifier) {
        return new Identifier(identifier);
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
        return "Operator{" +
                "op='" + op + '\'' +
                '}';
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
        return "Identifier{" +
                "identifier='" + identifier + '\'' +
                ", ref=" + ref +
                '}';
    }
}
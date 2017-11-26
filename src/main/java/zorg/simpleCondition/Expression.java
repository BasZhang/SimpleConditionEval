package zorg.simpleCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Expression {

    private List<Token> tokens;

    public Expression(String expression, Map<String, ICondition> context) {
        Objects.requireNonNull(expression);
        Objects.requireNonNull(context);
        this.tokens = tokenize(expression);
        bind(context);
    }

    private void bind(Map<String, ICondition> context) {
        for (Token token : tokens) {
            if (token instanceof Identifier) {
                Identifier identifier = (Identifier) token;
                identifier.ref = context.get(identifier.identifier);
            }
        }
    }

    private List<Token> tokenize(String expression) {
        List<Token> ans = new ArrayList<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (Character.isSpaceChar(c)) {
                continue;
            } else if (c == '&') {
                ans.add(Tokens.AND);
            } else if (c == '|') {
                ans.add(Tokens.OR);
            } else if (c == '!') {
                ans.add(Tokens.NOT);
            } else if (c == '?') {
                ans.add(Tokens.CONDITIONAL);
            } else if (c == ':') {
                ans.add(Tokens.OPTION);
            } else if (c == '(') {
                ans.add(Tokens.OPEN_PAREN);
            } else if (c == ')') {
                ans.add(Tokens.CLOSE_PAREN);
            } else if (c == '@') {
                ans.add(Tokens.REF);
            } else if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_') {
                StringBuilder stringBuilder = new StringBuilder();
                int j = i;
                for (; j < expression.length(); j++) {
                    char c1 = expression.charAt(j);
                    if (Character.isAlphabetic(c1) || Character.isDigit(c1) || c1 == '_') {
                        stringBuilder.append(c1);
                    } else {
                        break;
                    }
                }
                ans.add(Tokens.IDENTIFIER(stringBuilder.toString()));
                i = j;
            } else {
                throw new IllegalArgumentException("Unsupported character at " + i);
            }
        }
        return ans;
    }

    public boolean eval() {
        for (Token token : tokens) {

        }
        return false;
    }


}

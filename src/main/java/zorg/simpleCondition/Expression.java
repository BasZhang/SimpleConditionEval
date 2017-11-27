package zorg.simpleCondition;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class Expression {

    @SuppressWarnings("serial")
    class TokenList extends ArrayList<Token> {
        @Override
        public void removeRange(int fromIndex, int toIndex) {
            super.removeRange(fromIndex, toIndex);
        }
    }

    private final TokenList tokens;
    private final Map<String, ICondition> context;

    public Expression(String expression, Map<String, ICondition> context) {
        Objects.requireNonNull(expression);
        Objects.requireNonNull(context);
        this.tokens = tokenize(expression);
        this.context = context;
    }

    public boolean eval() {
        return evalByPrecedence(tokens);
    }

    private TokenList tokenize(String expression) {
        TokenList ans = new TokenList();

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
                stringBuilder.append(c);
                int j = i + 1;
                for (; j < expression.length(); j++) {
                    char c1 = expression.charAt(j);
                    if (Character.isAlphabetic(c1) || Character.isDigit(c1) || c1 == '_') {
                        stringBuilder.append(c1);
                        i++;
                    } else {
                        break;
                    }
                }
                ans.add(Tokens.IDENTIFIER(stringBuilder.toString()));
            } else {
                throw new IllegalArgumentException("Unsupported character at " + i);
            }
        }
        return ans;
    }

    private boolean evalByPrecedence(TokenList subTokens) {
        removeSurroundingParenPair(subTokens);

        for (int precedence = 0; precedence <= 6; precedence++) {
            for (int pos = 0; pos < subTokens.size(); pos++) {
                Token token = subTokens.get(pos);
                if (token instanceof Operator) {
                    Operator op = (Operator) token;
                    if (op.precedence == precedence) {
                        int lengthChange = operate(subTokens, pos);
                        pos += lengthChange;
                    }
                }
            }
        }
        if (subTokens.size() != 1 || !(subTokens.get(0) instanceof Identifier)) {
            throw new ExpressionException("expression format error");
        } else {
            return ((Identifier) subTokens.get(0)).ref.get();
        }
    }

    private void removeSurroundingParenPair(TokenList subTokens) {
        int size = subTokens.size();
        if (!subTokens.isEmpty() && (subTokens.get(0) == Tokens.OPEN_PAREN) && (subTokens.get(size - 1) == Tokens.CLOSE_PAREN)) {
            for (int i = 0, r = 0; i < size; i++) {
                Token tk3 = subTokens.get(i);
                if (tk3 == Tokens.OPEN_PAREN) {
                    r++;
                } else if (tk3 == Tokens.CLOSE_PAREN) {
                    if (--r == 0) {
                        if (i == size - 1) {
                            subTokens.remove(i);
                            subTokens.remove(0);
                        }
                        break;
                    }
                }
            }
        }
    }

    private int operate(TokenList subTokens, int pos) {
        int posChange = 0;
        Token tk = subTokens.get(pos);
        if (tk == Tokens.REF) {

            TokenList rightTks = removeRight(subTokens, pos);
            if (rightTks.size() != 1 || !(rightTks.get(0) instanceof Identifier)) {
                throw new ExpressionException("expression format error");
            }
            Identifier r = ((Identifier) rightTks.get(0));
            ICondition ref = context.get(r.identifier);
            if (ref == null) {
                throw new ExpressionException("can't bind @" + r.identifier);
            }
            r.ref = ref;
            subTokens.set(pos, r);

        } else if (tk == Tokens.NOT) {

            TokenList rightTks = removeRight(subTokens, pos);
            if (rightTks.isEmpty()) {
                throw new ExpressionException("expression format error");
            }

            boolean right = evalByPrecedence(rightTks);
            subTokens.set(pos, Tokens.VALUE(!right));

        } else if (tk == Tokens.AND) {

            TokenList leftTks = removeLeft(subTokens, pos);
            posChange = -leftTks.size();
            pos += posChange;
            TokenList rightTks = removeRight(subTokens, pos);
            if (leftTks.isEmpty() || rightTks.isEmpty()) {
                throw new ExpressionException("expression format error");
            }
            if (evalByPrecedence(leftTks)) {
                boolean right = evalByPrecedence(rightTks);
                subTokens.set(pos, Tokens.VALUE(right));
            } else {
                subTokens.set(pos, Tokens.VALUE(false));
            }

        } else if (tk == Tokens.OR) {

            TokenList leftTks = removeLeft(subTokens, pos);
            posChange = -leftTks.size();
            pos += posChange;
            TokenList rightTks = removeRight(subTokens, pos);
            if (leftTks.isEmpty() || rightTks.isEmpty()) {
                throw new ExpressionException("expression format error");
            }
            if (!evalByPrecedence(leftTks)) {
                boolean right = evalByPrecedence(rightTks);
                subTokens.set(pos, Tokens.VALUE(right));
            } else {
                subTokens.set(pos, Tokens.VALUE(true));
            }

        } else if (tk == Tokens.CONDITIONAL) {

            TokenList testTks = removeLeft(subTokens, pos);
            posChange = -testTks.size();
            pos += posChange;
            TokenList optionLeftTks = removeRight(subTokens, pos);
            if (testTks.isEmpty() || optionLeftTks.isEmpty()) {
                throw new ExpressionException("expression format error");
            }
            int colonPos = pos + 1;
            if (colonPos >= subTokens.size()) {
                throw new ExpressionException("expression format error");
            }
            Token shouldBeColon = subTokens.get(colonPos);
            if (shouldBeColon != Tokens.OPTION) {
                throw new ExpressionException("expression format error");
            }
            TokenList optionRightTks = removeRight(subTokens, colonPos);
            if (optionRightTks.isEmpty()) {
                throw new ExpressionException("expression format error");
            }
            subTokens.remove(colonPos);
            if (evalByPrecedence(testTks)) {
                boolean optionLeft = evalByPrecedence(optionLeftTks);
                subTokens.set(pos, Tokens.VALUE(optionLeft));
            } else {
                boolean optionRight = evalByPrecedence(optionRightTks);
                subTokens.set(pos, Tokens.VALUE(optionRight));
            }

        }
        return posChange;
    }

    private TokenList removeLeft(TokenList subTokens, int pos) {
        TokenList ans = new TokenList();
        int leftEndPos = pos - 1;
        if (leftEndPos < 0) {
            throw new ExpressionException("operator position error");
        }
        Token tkLeft = subTokens.get(leftEndPos);
        if (tkLeft instanceof Identifier) {
            subTokens.remove(leftEndPos);
            ans.add(tkLeft);
        } else if (tkLeft == Tokens.CLOSE_PAREN) {
            int r = 0;
            int foundPos = -1;
            for (int i = leftEndPos; i >= 0; i--) {
                if (subTokens.get(i) == Tokens.CLOSE_PAREN) {
                    r++;
                }
                if (subTokens.get(i) == Tokens.OPEN_PAREN) {
                    r--;
                    if (r == 0) {
                        foundPos = i;
                        break;
                    }
                }
            }
            if (foundPos != -1) {
                ans.addAll(subTokens.subList(foundPos, leftEndPos + 1));
                subTokens.removeRange(foundPos, leftEndPos + 1);
            } else {
                throw new ExpressionException("parentheses not match");
            }
        } else {
            throw new ExpressionException("expression format error");
        }
        return ans;
    }

    private TokenList removeRight(TokenList subTokens, int pos) {
        TokenList ans = new TokenList();
        int rightStartPos = pos + 1;
        int size = subTokens.size();
        if (rightStartPos >= size) {
            throw new ExpressionException("operator posisition error");
        }
        Token tkRight = subTokens.get(rightStartPos);
        if (tkRight instanceof Identifier) {
            subTokens.remove(rightStartPos);
            ans.add(tkRight);
        } else if (tkRight == Tokens.OPEN_PAREN) {
            int r = 0;
            int foundPos = -1;
            for (int i = rightStartPos; i < size; i++) {
                if (subTokens.get(i) == Tokens.OPEN_PAREN) {
                    r++;
                }
                if (subTokens.get(i) == Tokens.CLOSE_PAREN) {
                    r--;
                    if (r == 0) {
                        foundPos = i;
                        break;
                    }
                }
            }
            if (foundPos != -1) {
                ans.addAll(subTokens.subList(rightStartPos, foundPos + 1));
                subTokens.removeRange(rightStartPos, foundPos + 1);
            } else {
                throw new ExpressionException("parentheses not match");
            }
        } else {
            throw new ExpressionException("expression format error");
        }
        return ans;
    }

}
package com.interpreter.jlox;

/*
 * This class interprets expression and return results in the form of Java
 * Objects. It performs post-order traversal on the expressions.
 */
class Interpreter implements Expr.Visitor<Object> {

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTrue(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -((double) right);
        }

        // Unreachable.
        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    /*
     * The plus operator is overloaded to also support string concatenation.
     */
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
			    checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;

            case GREATER_EQUAL:
			    checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;

            case LESS:
			    checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;

            case LESS_EQUAL:
			    checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;

            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                throw new RuntimeError(expr.operator,
                    "Operands must be two numbers or two strings.");

            case MINUS:
			    checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;

            case STAR:
			    checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;

            case SLASH:
			    checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;

            case BANG_EQUAL:
                return !isEqual(left, right);

            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        // Unreachable.
        return null;
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    /*
     * This method tests if two Java objects are equal is the context of Lox
     * language. It behaves the same as Java `equals()`` test, except that it
     * also hanldes the case where the left is `null` (or `nil` in Lox).
     */
    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        } else if (left == null) {
            return false;
        } else {
            return left.equals(right);
        }
    }

    /*
     * This method evaluates Java objects to boolean. Boolean objects are
     * evaluated as they are. For other types, the following are defined as
     * false within their respective types:
     * - nil
     */
    private boolean isTrue(Object object) {
        if (object instanceof Boolean) return (boolean) object;
        if (object == null) return false;
        return true;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    /*
     * The method returns a string representation of a given object. The
     * convertion to string is consistent with Java except for:
     * - null, which is 'nil' in Lox;
     * - doubles that are actually integers, strip the ending '.0'.
     */
    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String repr = object.toString();
            if (repr.endsWith(".0")) {
                repr = repr.substring(0, repr.length() - 2);
            }
            return repr;
        }

        return object.toString();
    }

    void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError e) {
            Lox.runtimeError(e);
        }
    }
}

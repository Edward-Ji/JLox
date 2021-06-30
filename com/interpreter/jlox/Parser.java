package com.interpreter.jlox;

import java.util.ArrayList;
import java.util.List;

import static com.interpreter.jlox.TokenType.*;

/*
 * The `Parser` class performs **recursive descent parsing** which turns tokens
 * into statements. Each grammar rule in Lox is represented by a method.
 *
 * The parser is expected to:
 * - Given a valid sequence of tokens, produce a corresponding syntax tree.
 * - Given an invalid sequence of tokens, detect any errors and tell the user
 *   about their mistakes.
 *
 * This parser uses **panic mode error recovery** to synchronize.
 */

class Parser {

    private static class ParseError extends RuntimeException {}

    // A list of tokens used to synchronize in panic mode.
    private final List<TokenType> recoverTokenTypes = List.of(
        CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN
    );

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /*
     * The `parse()` method parses tokens into statements.
     *
     * program → declaration* EOF ;
     */
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    /*
     * declaration → varDecl
     *             | statement ;
     */
    private Stmt declaration() {
        try {
            if (match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError errors) {
            synchronize();
            return null;
        }
    }

    /*
     * varDecl → "var" IDENTIFIER ( "=" expression )? ";" ;
     */
    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    /*
     * statement → exprStmt
     *           | printStmt ;
     */
    private Stmt statement() {
        if (match(PRINT)) return printStatement();

        return expressionStatement();
    }

    /*
     * printStmt → "print" expression ";" ;
     */
    private Stmt printStatement() {
        Expr printExpr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Print(printExpr);
    }

    /*
    * exprStmt → expression ";" ;
    */
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    /*
     * TODO: Using Java 8, create a helper method for parsing a help method for
     * parsing a left-associative series of binary operators given a list of
     * token types, and an operand method handle to simplify this code.
     */

    /*
     * expression → equality ;
     */
    private Expr expression() {
        return equality();
    }

    /*
     * equality → comparison ( ( "!=" | "==" ) comparison )* ;
     */
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /*
     * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
     */
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /*
     * term → factor ( ( "-" | "+" ) factor )* ;
     */
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /*
     * factor → unary ( ( "/" | "*" ) unary )* ;
     */
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /*
     * unary → ( "!" | "-" ) unary
     *       | primary ;
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    /*
     * primary → NUMBER | STRING | "true" | "false" | "nil"
     *         | "(" expression ")" ;
     */
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE )) return new Expr.Literal(true );
        if (match(NIL  )) return new Expr.Literal(null );

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    /*
     * The `match()` method checks if the current token has any of the given
     * types. If so, it consume the token and returns `true`. Otherwise, it
     * just return `false`.
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /*
     * The `consume()` method checks if the next token is of the expected type.
     * If so, it consumes that token. Otherwise, it reports an error.
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    /*
     * The `check()` method returns `true` if the current token is of the given
     * type. Unlike `match()`, it never consumes the token.
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    /*
     * The `advance()` method consumes the current token and returns it.
     */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /*
     * The `isAtEnd()` method returns `true` if it reaches the last token, i.e.
     * EOF token type.
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /*
     * The `peek()` method returns the current token yet to be consumed.
     */
    private Token peek() {
        return tokens.get(current);
    }

    /*
     * The `previous()` method returns the most recently consumed token.
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /*
     * Report token error.
     */
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /*
     * The `synchronize()` method looks for a keyword that indicates the start
     * of the next statement. This method is called in panic mode.
     */
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            for (TokenType type : recoverTokenTypes) {
                if (peek().type == type) {
                    return;
                }
            }
        }

        advance();
    }
}

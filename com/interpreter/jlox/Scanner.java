package com.interpreter.jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.interpreter.jlox.TokenType.*;

class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND   );
        keywords.put("class",  CLASS );
        keywords.put("else",   ELSE  );
        keywords.put("false",  FALSE );
        keywords.put("for",    FOR   );
        keywords.put("fun",    FUN   );
        keywords.put("if",     IF    );
        keywords.put("nil",    NIL   );
        keywords.put("or",     OR    );
        keywords.put("print",  PRINT );
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER );
        keywords.put("this",   THIS  );
        keywords.put("true",   TRUE  );
        keywords.put("var",    VAR   );
        keywords.put("while",  WHILE );
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // Beginning of a lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single-character tokens.
            case '(': addToken(LEFT_PAREN ); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE ); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA      ); break;
            case '.': addToken(DOT        ); break;
            case '-': addToken(MINUS      ); break;
            case '+': addToken(PLUS       ); break;
            case ';': addToken(SEMICOLON  ); break;
            case '*': addToken(STAR       ); break;

            // One or two character tokens.
            case '!':
                addToken(match('=') ? BANG_EQUAL    : BANG   );
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL   : EQUAL  );
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL    : LESS   );
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            // Division operator or comment
            case '/':
                if (match('/')) {
                    // Skip single-line comment.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    longComment();
                } else {
                    addToken(SLASH);
                }
                break;

            // Ignore whitespaces.
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;

            // String literals.
            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    // Number literals.
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                    break;
                }
        }
    }

    // Handle token of identifiers.
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    // Handle token of number literals.
    private void number() {
        while (isDigit(peek())) advance();

        // Check for a fractional part.
        if (peek() == '.' && isDigit(peekMore())) {
            advance();  // consume the full stop
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    // Handle token of string literals.
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string literal.");
            return;
        }

        advance();  // skip the closing quotation mark

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    // Handle long comments.
    private void longComment() {
        while (!(peek() == '*' && peekMore() == '/') && !isAtEnd()) {
            if (advance() == '\n') line++;
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated multi-line comment.");
            return;
        }

        // Comsumer closing characters.
        advance();
        advance();
    }

    // Only advance and return if next char is as expected.
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        advance();
        return true;
    }

    // Look ahead and return that character.
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // Look ahead two characters and return it.
    private char peekMore() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isDigit(char c) {
        // Check if a character is strictly arabic numeral digits.
        return c >= '0' && c <= '9';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        // Object literal defaults to null.
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}

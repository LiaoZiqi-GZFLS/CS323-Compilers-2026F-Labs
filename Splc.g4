grammar Splc;

program
    : globalDef* EOF
    ;

globalDef
    : specifier Identifier LPAREN funcArgs RPAREN LBRACE statement* RBRACE      // 函数定义
    | specifier Identifier SEMI                                                  // 全局变量定义
    ;

specifier
    : INT
    | CHAR
    | VOID
    ;

funcArgs
    : (specifier Identifier (COMMA specifier Identifier)*)?
    ;

statement
    : LBRACE statement* RBRACE                                      # CombineStmt
    | specifier Identifier (ASSIGN expression)? SEMI                # LocalVariableStmt
    | RETURN expression SEMI                                        # ReturnStmt
    | expression SEMI                                               # ExpressionStmt
    ;

expression
    : LPAREN expression RPAREN
    | Identifier
    | NUMBER
    | CHAR_VALUE
    | expression (STAR | DIV ) expression
    | expression (PLUS | MINUS) expression
    | <assoc=right> expression ASSIGN expression
    ;


RETURN: 'return';
ASSIGN: '=';
PLUS: '+';
MINUS: '-';
STAR: '*';
DIV: '/';
SEMI: ';';
COMMA: ',';
LPAREN: '(';
RPAREN: ')';
LBRACE: '{';
RBRACE: '}';
LBRACK: '[';
RBRACK: ']';
INT: 'int';
CHAR: 'char';
VOID: 'void';
Identifier : [a-zA-Z_] [a-zA-Z_0-9]* ;
NUMBER       : [0-9]+ ;
CHAR_VALUE: '\'' ( EscapeChar | ~['\\] ) '\'';
fragment EscapeChar: '\\' ( 'n' | 't' | '\'' | '\\' | '0' );
WS           : [ \t\r\n]+ -> skip ;
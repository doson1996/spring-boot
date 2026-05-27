grammar Aql;

// 顶层查询
query
    : forStatement+ returnStatement EOF
    ;

// FOR 循环（可以有多个，表示隐式连接或子查询）
forStatement
    : FOR variable (',' variable)? IN expression          # ForInExpression
    | FOR variable (',' variable)? IN minDepth '..' maxDepth direction expression (GRAPH StringLiteral)?  # ForGraphTraversal
    ;

// 方向
direction
    : OUTBOUND
    | INBOUND
    | ANY
    ;

// 深度
minDepth : IntegerLiteral ;
maxDepth : IntegerLiteral ;

// 表达式（子集，包含子查询、对象、数组、函数调用等）
expression
    : '(' query ')'                                 # SubqueryExpr
    | objectLiteral                                 # ObjectLiteralExpr
    | arrayLiteral                                  # ArrayLiteralExpr
    | functionCall                                  # FunctionCallExpr
    | variable                                      # VariableExpr
    | StringLiteral                                 # StringLiteralExpr
    | IntegerLiteral                                # IntegerLiteralExpr
    | FloatLiteral                                  # FloatLiteralExpr
    | BooleanLiteral                                # BooleanLiteralExpr
    | '(' expression ')'                            # ParenExpr
    | expression op=('*'|'/'|'%') expression        # MulDivExpr
    | expression op=('+'|'-') expression            # AddSubExpr
    | expression op=('=='|'!='|'<'|'>'|'<='|'>=') expression # ComparisonExpr
    | expression '&&' expression                    # AndExpr
    | expression '||' expression                    # OrExpr
    | NOT expression                                # NotExpr
    ;

objectLiteral
    : '{' (property (',' property)*)? '}'
    ;

property
    : (StringLiteral | Identifier) ':' expression
    ;

arrayLiteral
    : '[' (expression (',' expression)*)? ']'
    ;

functionCall
    : Identifier '(' (expression (',' expression)*)? ')'
    ;

// 中间操作（FILTER / LET / SORT / LIMIT / COLLECT）
// 这些可以出现在 FOR 之后，任意顺序
operation
    : FILTER expression                             # FilterOp
    | LET variable '=' expression                  # LetOp
    | SORT expression (ASC | DESC)?                 # SortOp
    | LIMIT IntegerLiteral (',' IntegerLiteral)?    # LimitOp
    | COLLECT collectVariable '=' expression INTO variable? (KEEP Identifier?)?  # CollectOp
    ;

// 注意：FOR 之后可以连续出现多个 operation，最后必须跟 RETURN
forBody
    : operation* returnStatement
    ;

// RETURN 语句
returnStatement
    : RETURN expression
    | RETURN DISTINCT expression
    ;

// 词汇规则
OUTBOUND : O U T B O U N D ;
INBOUND  : I N B O U N D ;
ANY      : A N Y ;
FOR      : F O R ;
IN       : I N ;
FILTER   : F I L T E R ;
LET      : L E T ;
SORT     : S O R T ;
LIMIT    : L I M I T ;
RETURN   : R E T U R N ;
DISTINCT : D I S T I N C T ;
COLLECT  : C O L L E C T ;
INTO     : I N T O ;
KEEP     : K E E P ;
ASC      : A S C ;
DESC     : D E S C ;
GRAPH    : G R A P H ;
AND      : '&&' ;
OR       : '||' ;
NOT      : N O T ;

// 标识符和变量
variable
    : Identifier
    | Identifier '.' Identifier                   // 路径访问
    | Identifier '[' expression ']'               // 索引访问
    ;

Identifier
    : [a-zA-Z_][a-zA-Z_0-9]*
    ;

StringLiteral
    : '\'' (~['\\] | '\\' .)* '\''
    | '"' (~["\\] | '\\' .)* '"'
    ;

IntegerLiteral
    : [0-9]+
    ;

FloatLiteral
    : [0-9]+ '.' [0-9]+
    ;

BooleanLiteral
    : 'true' | 'false'
    ;

// 跳过空白和注释
WS : [ \t\r\n]+ -> skip ;
COMMENT : '//' ~[\r\n]* -> skip ;
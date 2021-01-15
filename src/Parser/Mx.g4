grammar Mx;

program: programSub* EOF;
programSub: varDef | funcDef | classDef;

varDef : type varDefSub (',' varDefSub)* ';';
varDefSub : Identifier ('=' expression)?;
funcDef : returnType? Identifier '(' paramList? ')' block;
classDef : Class Identifier '{' (varDef | funcDef)* '}' ';';

paramList : param (',' param)*;
param : type Identifier;

type : simpleType ('[' ']')*;
simpleType : Int | Bool | String | Identifier;
returnType : type | Void;

block : '{' statement* '}';

primary : '(' expression ')' | This | Identifier | literal;
literal : IntLiteral | BoolLiteral | StrLiteral | NullLiteral;

creator
    : simpleType ('[' expression ']')+ ('[' ']')+ ('[' expression ']')+     #errorCreator
    | simpleType ('[' expression ']')+ ('[' ']')*                           #arrayCreator
    | simpleType ('(' ')')                                                  #classCreator
    | simpleType                                                            #simpleCreator
    ;

statement
    : block                                                                             #blockStmt
    | varDef                                                                            #varDefStmt
    | If '(' expression ')' trueStmt=statement (Else falseStmt=statement)?              #ifStmt
    | While '(' expression ')' statement                                                #whileStmt
    | For '(' init=expression? ';' cond=expression? ';' inc=expression? ')' statement   #forStmt
    | Break ';'                                                                         #breakStmt
    | Continue ';'                                                                      #continueStmt
    | Return expression? ';'                                                            #returnStmt
    | expression ';'                                                                    #pureExprStmt
    | ';'                                                                               #emptyStmt
    ;

expression
    : primary                                                       #atomExpr
    | expression '(' expressionList? ')'                            #funcCallExpr
    | base=expression '[' offset=expression ']'                     #subscriptExpr
    | expression '.' Identifier                                     #memberExpr
    | <assoc=right> New creator                                     #newExpr
    | expression op=('++' | '--')                                   #suffixExpr
    | <assoc=right> op=('++' | '--') expression                     #prefixExpr
    | <assoc=right> op=('+' | '-') expression                       #prefixExpr
    | <assoc=right> op=('!' | '~') expression                       #prefixExpr
    | src1=expression op=('*' | '/' | '%') src2=expression          #binaryExpr
    | src1=expression op=('+' | '-') src2=expression                #binaryExpr
    | src1=expression op=('<<' | '>>') src2=expression              #binaryExpr
    | src1=expression op=('<' | '>' | '<=' | '>=') src2=expression  #binaryExpr
    | src1=expression op=('==' | '!=') src2=expression              #binaryExpr
    | src1=expression op='&' src2=expression                        #binaryExpr
    | src1=expression op='^' src2=expression                        #binaryExpr
    | src1=expression op='|' src2=expression                        #binaryExpr
    | src1=expression op='&&' src2=expression                       #binaryExpr
    | src1=expression op='||' src2=expression                       #binaryExpr
    | <assoc=right> src1=expression op='=' src2=expression          #binaryExpr
    ;
expressionList : expression (',' expression)*;

IntLiteral : [1-9] [0-9]* | '0';
StrLiteral : '"' (~["\\\n\r] | '\\' ["\\nr])* '"';
BoolLiteral : True | False;
NullLiteral : Null;

Int : 'int';
Bool : 'bool';
String : 'string';
Null : 'null';
Void : 'void';
True : 'true';
False : 'false';
If : 'if';
Else : 'else';
For : 'for';
While : 'while';
Break : 'break';
Continue : 'continue';
Return : 'return';
New : 'new';
Class : 'class';
This : 'this';

Identifier : [a-zA-Z] [a-zA-Z_0-9]*;

Whitespace : [ \t]+ -> skip;
Newline : ('\r' '\n'? | '\n') -> skip;
BlockComment : '/*' .*? '*/' -> skip;
LineComment : '//' ~[\r\n]* -> skip;

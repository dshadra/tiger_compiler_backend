grammar IR;

// Parser
program
    : IRPROGRAM ID declarationSegment functList ENDPROGRAM EOF
    ;

declarationSegment
    : INTDECL COLON intList=valueList? FLOATDECL COLON floatList=valueList?
    ;

valueList
    : value (COMMA value)*
    ;

type
    : INT
    | FLOAT
    ;

functList
    :   function*
    ;

function
    : FUNCTION name=ID OPENPAREN paramList? CLOSEPAREN retType declarationSegment statSeq ENDFUNCTION
    ;

paramList
    : param (COMMA param)*
    ;

param
    : type ID
    ;

retType
    : COLON (type | VOID)
    ;

statSeq
    : stat+
    ;

staticAssignmentStatSeq
    : stat*
    ;

branchCondition
    : BRANCHEQUAL
    | BRANCHGREATERTHAN
    | BRANCHGREATERTHANOREQUALTO
    | BRANCHLESSTHAN
    | BRANCHLESSTHANOREQUALTO
    | BRANCHNOTEQUAL
    ;

operation
    : ADD
    | SUBSTRACT
    | MULTIPLY
    | DIVIDE
    | AND
    | OR
    ;


stat
    : ASSIGN COMMA leftExp=expr COMMA rightExp=expr                              #stat_assign
    | branchCondition COMMA leftExp=expr COMMA rightExp=expr COMMA label=ID      #stat_condition
    | operation COMMA leftExp=expr COMMA rightExp=expr COMMA result=value        #stat_math
    | ARRAYSTORE COMMA arrayName=ID COMMA index=expr COMMA source=expr           #stat_arraystore
    | ARRAYLOAD COMMA destination=ID COMMA arrayName=ID COMMA index=expr         #stat_arrayload
    | CALL COMMA funtionName=ID (COMMA exprList)?                                #stat_call
    | CALLRETURN COMMA returnValue=value COMMA funtionName=ID (COMMA exprList)?  #stat_callreturn
    | GOTO COMMA label=ID                                                        #stat_goto
    | RETURN (COMMA expr)?                                                       #stat_return
    | label=ID COLON                                                             #stat_label
    ;

expr
    : constant                                                          #expr_constant
    | value                                                             #expr_value
    ;

value
    : ID                                    #value_id
    | ID OPENBRACK expr CLOSEBRACK          #value_array
    ;


exprList
    : expr (COMMA expr)*
    ;

constant
    : INTLIT
    | FLOATLIT
    ;

//Scanner
ADD                         :   'add' ;
AND                         :   'and' ;
ARRAYSTORE                  :   'array_store' ;
ARRAYLOAD                   :   'array_load' ;
ASSIGN                      :   'assign' ;
BRANCHEQUAL                 :   'breq' ;
BRANCHGREATERTHAN           :   'brgt' ;
BRANCHGREATERTHANOREQUALTO  :   'brgeq' ;
BRANCHLESSTHAN              :   'brlt' ;
BRANCHLESSTHANOREQUALTO     :   'brleq' ;
BRANCHNOTEQUAL              :   'brneq' ;
CALL                        :   'call' ;
CALLRETURN                  :   'callr' ;
DIVIDE                      :   'div' ;
ENDFUNCTION                 :   'end_function' ;
ENDPROGRAM                  :   'end_program' ;
FLOAT                       :   'float' ;
FLOATDECL                   :   'float-list' ;
FUNCTION                    :   'start_function' ;
GOTO                        :   'goto' ;
INT                         :   'int' ;
INTDECL                     :   'int-list' ;
IRPROGRAM                   :   'start_program' ;
MULTIPLY                    :   'mult' ;
OR                          :   'or' ;
RETURN                      :   'return' ;
SUBSTRACT                   :   'sub' ;
VOID                        :   'void' ;


ID       :  [a-zA-Z_] [a-zA-Z0-9_]* ;
INTLIT   :  '0' | [1-9] [0-9]* ;
FLOATLIT :  INTLIT '.' [0-9]* ;


COMMA      :   ',' ;
COLON      :   ':' ;
OPENPAREN  :   '(' ;
CLOSEPAREN :   ')' ;
OPENBRACK  :   '[' ;
CLOSEBRACK :   ']' ;


COMMENT :   '/*' .*? '*/' -> skip ;
WS      :   [ \t\r\n]+ -> skip ;

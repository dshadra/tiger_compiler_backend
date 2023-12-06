import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NaiveMipsCodeGeneratorListener extends IRBaseListener {
    List<String> mipsCode = new ArrayList<String>();
    Map<String, String> errors = new HashMap<String, String>();

    int stackIndex = 0;
    int staticVariableIndex = 0;

    int currentStackFrameSize = 1;

    int stackReserveInstructionIndex;

    int maxFunctionParams = 0;

    String returnType;

    Map<String, SymbolInfo> symbolsMap;

    Map<String, SymbolInfo> globalSymbols;

    public List<String> getMipsCode() {
        return mipsCode;
    }

    public void setMipsCode(List<String> mipsCode) {
        this.mipsCode = mipsCode;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public String indent = "";

    boolean isFunction = false;
    String currentFunctionName = "";

    String initializer = "0";


    @Override public void enterProgram(IRParser.ProgramContext ctx) {
        mipsCode.add(".data # static data section");
    }

    @Override public void enterDeclarationSegment(IRParser.DeclarationSegmentContext ctx) {
        int intCount = 0;
        int floatCount = 0;
        if(isFunction) {
            if(ctx.intList != null) {
                List<ParseTree> parseTree = ctx.intList.children;
                if(parseTree != null && !parseTree.isEmpty()) {
                    for(ParseTree p: parseTree) {
                        if(p instanceof IRParser.Value_idContext) {
                            intCount++;
                            SymbolInfo s = new SymbolInfo();
                            s.setType("int");
                            s.setOffset(stackIndex++);
                            s.setBaseLabel(((IRParser.Value_idContext)p).ID().getText());
                            s.setStorage(SymbolInfo.Storage.LOCAL);
                            s.setStackAreaAddressLabel("varAreaStart + ");
                            symbolsMap.put(((IRParser.Value_idContext)p).ID().getText(), s);
                        } else if(p instanceof IRParser.Value_arrayContext) { // Integer Array
                            IRParser.Value_arrayContext arrayContext = (IRParser.Value_arrayContext)p;
                            int dimension = Integer.parseInt(arrayContext.expr().getText());
                            intCount = intCount + dimension;
                            SymbolInfo s = new SymbolInfo();
                            s.setType("int");
                            s.setOffset(stackIndex++);
                            s.setBaseLabel(arrayContext.ID().getText());
                            s.setStorage(SymbolInfo.Storage.LOCAL);
                            s.setStackAreaAddressLabel("varAreaStart + ");
                            s.setDimensions(dimension);
                            symbolsMap.put(arrayContext.ID().getText(), s);
                        }
                    }
                }
            }
            if(ctx.floatList != null) {
                List<ParseTree> parseTree = ctx.floatList.children;
                if(parseTree != null && !parseTree.isEmpty()) {
                    for(ParseTree p: parseTree) {
                        if(p instanceof IRParser.Value_idContext) {
                            floatCount++;
                            SymbolInfo s = new SymbolInfo();
                            s.setType("float");
                            s.setOffset(stackIndex++);
                            s.setBaseLabel(((IRParser.Value_idContext)p).ID().getText());
                            s.setStorage(SymbolInfo.Storage.LOCAL);
                            s.setStackAreaAddressLabel("varAreaStart + ");
                            symbolsMap.put(((IRParser.Value_idContext)p).ID().getText(), s);
                        } else if(p instanceof IRParser.Value_arrayContext) {
                            IRParser.Value_arrayContext arrayContext = (IRParser.Value_arrayContext)p;
                            int dimension = Integer.valueOf(arrayContext.expr().getText());
                            floatCount = floatCount + dimension;
                            SymbolInfo s = new SymbolInfo();
                            s.setType("float");
                            s.setOffset(stackIndex++);
                            s.setBaseLabel(arrayContext.ID().getText());
                            s.setStorage(SymbolInfo.Storage.LOCAL);
                            s.setStackAreaAddressLabel("varAreaStart + ");
                            s.setDimensions(dimension);
                            symbolsMap.put(arrayContext.ID().getText(), s);
                        }
                    }
                }
            }
            //TODO - stack size should be divisibe by 8
            currentStackFrameSize = currentStackFrameSize + (intCount + floatCount);
        } else { //static declaration
            globalSymbols= new HashMap<String, SymbolInfo>();
            if(ctx.intList != null) {
                List<ParseTree> parseTree = ctx.intList.children;
                if(parseTree != null && !parseTree.isEmpty()) {
                    for(ParseTree p: parseTree) {
                        initializer = "0";
                        if(p instanceof IRParser.Value_idContext) {
                            SymbolInfo s = new SymbolInfo();
                            s.setType("int");
                            s.setBaseLabel(((IRParser.Value_idContext)p).ID().getText());
                            s.setStorage(SymbolInfo.Storage.STATIC);
                            globalSymbols.put(((IRParser.Value_idContext)p).ID().getText(), s);
                            String staticVariable = ((IRParser.Value_idContext)p).ID().getText();
                            mipsCode.add(indent + staticVariable + ": .word " + initializer);
                        } else if(p instanceof IRParser.Value_arrayContext) {
                            IRParser.Value_arrayContext arrayContext = (IRParser.Value_arrayContext)p;
                            int dimension = Integer.parseInt(arrayContext.expr().getText());
                            for(int i = 0; i < dimension-1; i++) {
                                initializer = initializer + ", 0";
                            }
                            SymbolInfo s = new SymbolInfo();
                            s.setType("int");
                            s.setBaseLabel(arrayContext.ID().getText());
                            s.setStorage(SymbolInfo.Storage.STATIC);
                            s.setDimensions(dimension);
                            globalSymbols.put(arrayContext.ID().getText(), s);
                            String staticVariable = arrayContext.ID().getText();
                            mipsCode.add(indent + staticVariable + ": .word " + initializer);
                        }
                    }
                }
            }
            if(ctx.floatList != null) {
                List<ParseTree> parseTree = ctx.floatList.children;
                if(parseTree != null && !parseTree.isEmpty()) {
                    for(ParseTree p: parseTree) {
                        initializer = "0.0";
                        if(p instanceof IRParser.Value_idContext) {
                            SymbolInfo s = new SymbolInfo();
                            s.setType("float");
                            s.setBaseLabel(((IRParser.Value_idContext)p).ID().getText());
                            s.setStorage(SymbolInfo.Storage.STATIC);
                            globalSymbols.put(((IRParser.Value_idContext)p).ID().getText(), s);
                            String staticVariable = ((IRParser.Value_idContext)p).ID().getText();
                            mipsCode.add(indent + staticVariable + ": .float " + initializer);
                        } else if(p instanceof IRParser.Value_arrayContext) {
                            IRParser.Value_arrayContext arrayContext = (IRParser.Value_arrayContext)p;
                            int dimension = Integer.parseInt(arrayContext.expr().getText());
                            for(int i = 0; i < dimension-1; i++) {
                                initializer = initializer + ", 0.0";
                            }
                            SymbolInfo s = new SymbolInfo();
                            s.setType("float");
                            s.setBaseLabel(arrayContext.ID().getText());
                            s.setStorage(SymbolInfo.Storage.STATIC);
                            s.setDimensions(dimension);
                            globalSymbols.put(arrayContext.ID().getText(), s);
                            String staticVariable = arrayContext.ID().getText();
                            mipsCode.add(indent + staticVariable + ": .float " + initializer);
                        }
                    }
                }
            }
            mipsCode.add(".text # code section");
            mipsCode.add(".globl main");
        }
    }

    @Override public void enterFunction(IRParser.FunctionContext ctx) {
        symbolsMap = new HashMap<String, SymbolInfo>();
        stackIndex = 0;
        returnType = "";

        currentStackFrameSize = 1;
        currentFunctionName = ctx.ID().getText();
        isFunction = true;
        returnType = ctx.retType().type()!=null?ctx.retType().type().getText():"void";
        mipsCode.add(currentFunctionName + ":");
        maxFunctionParams = 0;
        indent = "\t";
        IRParser.ParamListContext paramListCtx = ctx.paramList();

        if(paramListCtx != null) {
            List<ParseTree> parseTreeList = paramListCtx.children;
            int paramIndex = 1;
            for(ParseTree p : parseTreeList) {
                if(p instanceof  IRParser.ParamContext) {
                    currentStackFrameSize = currentStackFrameSize + 1;
                    IRParser.ParamContext param = (IRParser.ParamContext)p;
                    SymbolInfo s = new SymbolInfo();
                    s.setType(param.type().getText());
                    s.setOffset((1+paramIndex)*4);
                    s.setBaseLabel(param.ID().getText());
                    s.setStorage(SymbolInfo.Storage.LOCAL);
                    s.setStackAreaAddressLabel("frameSize - ");
                    symbolsMap.put(param.ID().getText(), s);
                    mipsCode.add(indent + "lw $t0 , " + (4*(paramIndex-1))+ "( $sp ) # read param value from caller stack frame" + paramIndex);
                    mipsCode.add(indent + "sw $t0 , -" + (4*(paramIndex+1))+"( $sp ) # store param value to callee stack frame" + paramIndex);
//                variableToMemoryMap.put(param.ID().getText(), "frameSize - " + (4*(paramIndex+1)));
                    paramIndex++;
                }
            }
        }
        mipsCode.add(indent + "addiu $sp , $sp ,  # placeholder instruction to reserve room on the stack");
        mipsCode.add(indent + "sw $ra , ( $sp )" + " # placeholder instruction to save return address");
        stackReserveInstructionIndex = mipsCode.size()-1;
    }

    @Override public void exitFunction(IRParser.FunctionContext ctx) {

        //replace
        currentStackFrameSize = currentStackFrameSize + maxFunctionParams;
        mipsCode.set(stackReserveInstructionIndex-1, indent + "addiu $sp , $sp , -" + (4*currentStackFrameSize) + " # reserve room on the stack");
        mipsCode.set(stackReserveInstructionIndex, indent + "sw $ra , " + (4*(currentStackFrameSize-1)) + "( $sp ) # store return address");
        mipsCode.add(indent + "lw $ra , " + (4*(currentStackFrameSize-1)) + "( $sp ) # restore return address");
        mipsCode.add(indent + "addiu $sp , $sp , " + (4*currentStackFrameSize) + " # restore stack");
        mipsCode.add(indent + "jr $ra");
        //adjust instructions with frame size
        for(int i = mipsCode.size()-1; i >= 0; i--) {
            String instruction = mipsCode.get(i);
            if(instruction.equalsIgnoreCase(currentFunctionName + ":")) {
                break;
            }
            if(instruction.contains("frameSize")) {
                String[] str = instruction.split("frameSize - ");
                int j = str[1].indexOf("(");
                mipsCode.set(i, str[0] + ((4*currentStackFrameSize)-Integer.parseInt(str[1].substring(0, j))) + str[1].substring(j));
            } else if(instruction.contains("varAreaStart")) {
                String[] str = instruction.split("varAreaStart \\+ ");
                int j = str[1].indexOf("(");
                mipsCode.set(i, str[0] + (4*(maxFunctionParams+Integer.parseInt(str[1].substring(0, j)))) + str[1].substring(j));
            }
        }
        indent = "";
    }


    /*
    ARRAYSTORE COMMA arrayName=ID COMMA index=INTLIT COMMA expr
    load source variable to a register
    load index to a register
    store source to array at index
    */
    @Override public void enterStat_arraystore(IRParser.Stat_arraystoreContext ctx) {
        boolean isArrayInt = true;
        SymbolInfo array = symbolsMap.get(ctx.arrayName.getText());
        if(array == null) array = globalSymbols.get(ctx.arrayName.getText());
        String registerName;
        if (array.getType().equalsIgnoreCase("float")) {
            registerName = "$f4";
            isArrayInt = false;
        } else {
            registerName = "$t0";
        }
        //load source expr to register
        IRParser.ExprContext sourceExprCtx = ctx.source;
        List<ParseTree> parseTree = sourceExprCtx.children;
        if (parseTree != null && !parseTree.isEmpty() && parseTree.size() == 1) {
            ParseTree p = parseTree.get(0);
            if (p instanceof IRParser.ConstantContext) {
                if(((IRParser.ConstantContext) p).INTLIT() != null) {
                    if (isArrayInt) {
                        mipsCode.add(indent + "li " + registerName + ", " + p.getText() + " #load int source constant");
                    } else {
                        mipsCode.add(indent + "li.s " + registerName + ", " + p.getText() + ".0 #load int source constant");
                        mipsCode.add(indent + "cvt.s.w  " + registerName + ", " + registerName + " #covert int constant to float");
                    }
                } else {
                    mipsCode.add(indent + "li.s " + registerName + ", " + p.getText() + " #load float source constant");
                }
            } else {
                SymbolInfo source = symbolsMap.get(p.getText());
                if(source == null) source = globalSymbols.get(p.getText());
                if(source.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                    if(source.getType().equalsIgnoreCase("int")) {
                        if (isArrayInt) {
                            mipsCode.add(indent + "li " + registerName + ", " + source.getBaseLabel());
                        } else {
                            mipsCode.add(indent + "li.s " + registerName + ", " + source.getBaseLabel());
                        }
                    } else {
                        mipsCode.add(indent + "li.s " + registerName + ", " + source.getBaseLabel());
                    }
                } else {
                    if (source.getType().equalsIgnoreCase("int")) {
                        if (isArrayInt) {
                            mipsCode.add(indent + "lw " + registerName + ", " + source.getStackAreaAddressLabel() + source.getOffset() + "($sp)");
                        } else {
                            mipsCode.add(indent + "l.s " + registerName + ", " + source.getStackAreaAddressLabel() + source.getOffset() + "($sp)");
                        }
                    } else {
                        mipsCode.add(indent + "l.s " + registerName + ", " + source.getStackAreaAddressLabel() + source.getOffset() + "($sp)");
                    }
                }
                if(source.getType().equalsIgnoreCase("int") && !isArrayInt) {
                    mipsCode.add(indent + "cvt.s.w  " + registerName + ", " + registerName + " #covert int value to float");
                }
            }
        }

        //load index expr to register $t1
        IRParser.ExprContext indexExprCtx = ctx.index;
        parseTree = indexExprCtx.children;
        if (parseTree != null && !parseTree.isEmpty() && parseTree.size() == 1) {
            ParseTree p = parseTree.get(0);
            if (p instanceof IRParser.ConstantContext) {
                mipsCode.add(indent + "li $t1, " + p.getText() + " #load index constant");
                mipsCode.add(indent + "sll $t1, $t1, 2 #index multiplication by 4");
            } else {
                SymbolInfo index = symbolsMap.get(p.getText());
                if (index == null) index = globalSymbols.get(p.getText());
                if(index.getType().equalsIgnoreCase("int")) {
                    if (index.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                        mipsCode.add(indent + "lw $t1, " + index.getBaseLabel());
                        mipsCode.add(indent + "sll $t1, $t1, 2 #index multiplication by 4");
                    } else {
                        mipsCode.add(indent + "lw  $t1," + index.getStackAreaAddressLabel() + index.getOffset() + "($sp)");
                        mipsCode.add(indent + "sll $t1, $t1, 2 #index multiplication by 4");
                    }
                } else {
                    throw new RuntimeException("index to array cannot be a float");
                }
            }
        }

        //store to value in register to array
        if(array.getStorage().equals(SymbolInfo.Storage.STATIC)) {
            mipsCode.add(indent + "la $t2, " + array.getBaseLabel());
            mipsCode.add(indent + "addu $t1 , $t1 , $t2 # index + base for array store");
            mipsCode.add(indent + "sw " + registerName + ", 0($t1)");
        } else {
            mipsCode.add(indent + "addu $t1 , $t1 , $sp # stack start + index");
            mipsCode.add(indent + "sw " + registerName + ", " + array.getStackAreaAddressLabel() + array.getOffset() + "($t1)");
        }
    }

    /**
    ARRAYSTORE COMMA arrayName=ID COMMA index=INTLIT COMMA expr
    load source variable to a register
    load index to a register
    store source to dest
    */
    @Override public void enterStat_arrayload(IRParser.Stat_arrayloadContext ctx) {
//    ARRAYLOAD COMMA leftExp=ID COMMA arrayName=ID COMMA index=INTLIT      #stat_arrayload
        boolean isDestInt = true;
        SymbolInfo array = symbolsMap.get(ctx.arrayName.getText());
        if(array == null) array = globalSymbols.get(ctx.arrayName.getText());
        if(array != null) {
            //load index expr to register $t1
            IRParser.ExprContext indexExprCtx = ctx.index;
            List<ParseTree> parseTree = indexExprCtx.children;
            if (parseTree != null && !parseTree.isEmpty() && parseTree.size() == 1) {
                ParseTree p = parseTree.get(0);
                if (p instanceof IRParser.ConstantContext) {
                    mipsCode.add(indent + "li $t1, " + p.getText() + " #load index constant");
                    mipsCode.add(indent + "sll $t1, $t1, 2 #index multiplication by 4");
                } else {
                    SymbolInfo index = symbolsMap.get(p.getText());
                    if (index == null) index = globalSymbols.get(p.getText());
                    if(index.getType().equalsIgnoreCase("int")) {
                        if (index.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                            mipsCode.add(indent + "lw $t1, " + index.getBaseLabel());
                            mipsCode.add(indent + "sll $t1, $t1, 2 #index multiplication by 4");
                        } else {
                            mipsCode.add(indent + "lw  $t1," + index.getStackAreaAddressLabel() + index.getOffset() + "($sp)");
                            mipsCode.add(indent + "sll $t1, $t1, 2 #index multiplication by 4");
                        }
                    } else {
                        throw new RuntimeException("index to array cannot be a float");
                    }
                }
            }

            String destination = ctx.destination.getText();
            SymbolInfo destSymbol = symbolsMap.get(destination);
            if(destSymbol == null) destSymbol = globalSymbols.get(destination);
            String registerName;
            if (destSymbol.getType().equalsIgnoreCase("float")) {
                registerName = "$f4";
                isDestInt = false;
            } else {
                registerName = "$t0";
            }

            //load array value to register
            if(array.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                mipsCode.add(indent + "la $t2, " + array.getBaseLabel());
                mipsCode.add(indent + "addu $t1 , $t1 , $t2 # index + base for array load");
                if(isDestInt) {
                    mipsCode.add(indent + "lw " + registerName + ", 0($t1)");
                } else {
                    if(array.getType().equalsIgnoreCase("int")) {
                        mipsCode.add(indent + "l.s " + registerName + ", 0($t1)");
                        mipsCode.add(indent + "cvt.s.w  " + registerName + ", " + registerName + " #convert int value to float");
                    } else {
                        mipsCode.add(indent + "l.s " + registerName + ", 0($t1)");
                    }
                }
            } else {
                mipsCode.add(indent + "addu $t1 , $t1 , $sp # stack start + index");
                if(isDestInt) {
                    mipsCode.add(indent + "lw " + registerName + ", " + array.getStackAreaAddressLabel() + array.getOffset() + "($t1)");
                } else {
                    if(array.getType().equalsIgnoreCase("int")) {
                        mipsCode.add(indent + "l.s " + registerName + ", " + array.getStackAreaAddressLabel() + array.getOffset() + "($t1)");
                        mipsCode.add(indent + "cvt.s.w  " + registerName + ", " + registerName + " #covert int value to float");
                    } else {
                        mipsCode.add(indent + "l.s " + registerName + ", " + array.getStackAreaAddressLabel() + array.getOffset() + "($t1)");
                    }
                }
            }


            //store value in register to destination
            if(destSymbol.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                if (isDestInt) {
                    mipsCode.add(indent + "la $t1, " + destSymbol.getBaseLabel());
                    mipsCode.add(indent + "sw " + registerName + ", 0($t1)");
                } else {
                    mipsCode.add(indent + "la $t1, " + destSymbol.getBaseLabel());
                    mipsCode.add(indent + "s.s " + registerName + ", 0($t1)");
                }
            } else {
                if (isDestInt) {
                    mipsCode.add(indent + "sw " + registerName + ", " + destSymbol.getStackAreaAddressLabel() + ( destSymbol.getOffset()+ "($sp)"));
                } else {
                    mipsCode.add(indent + "s.s " + registerName + ", " + destSymbol.getStackAreaAddressLabel() + ( destSymbol.getOffset()+ "($sp)"));
                }
            }
        }
    }


    @Override public void enterStat_call(IRParser.Stat_callContext ctx) {
        int numberOfCallParams = 0;
        if(ctx.funtionName.getText().equalsIgnoreCase("printi")) {
            IRParser.ExprListContext e = ctx.exprList();
            List<ParseTree> parseTree = e.children;
            if(parseTree != null && !parseTree.isEmpty()) {
                if(parseTree.get(0) instanceof IRParser.Expr_constantContext) {
                    String variable = ctx.exprList().getText();
                    mipsCode.add(indent + "li $a0, " + variable);
                } else {
                    String variable = ctx.exprList().getText();
                    SymbolInfo var = symbolsMap.get(variable);
                    if(var == null) var = globalSymbols.get(variable);
                    if(var.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                        mipsCode.add(indent + "lw $a0, " + var.getBaseLabel());
                    } else {
                        mipsCode.add(indent + "lw $a0, " + var.getStackAreaAddressLabel() + var.getOffset()+ "($sp)");
                    }
                }
            }
//            mipsCode.add(indent + "move $a0 , $t0"  +" # printi (t0)");
            mipsCode.add(indent + "li $v0 , 1");
            mipsCode.add(indent + "syscall");
            //print new line
            mipsCode.add(indent + "li $a0, 10");
            mipsCode.add(indent + "li $v0 , 11");
            mipsCode.add(indent + "syscall");
        } else if(ctx.funtionName.getText().equalsIgnoreCase("printf")) {
            IRParser.ExprListContext e = ctx.exprList();
            List<ParseTree> parseTree = e.children;
            if(parseTree != null && !parseTree.isEmpty()) {
                if(parseTree.get(0) instanceof IRParser.Expr_constantContext) {
                    String variable = ctx.exprList().getText();
                    if(((IRParser.Expr_constantContext)parseTree.get(0)).constant().INTLIT() != null) {
                        variable = variable + ".0";
                    }
                    mipsCode.add(indent + "li.s $f12, " + variable);
                } else {
                    String variable = ctx.exprList().getText();
                    SymbolInfo var = symbolsMap.get(variable);
                    if(var == null) var = globalSymbols.get(variable);
                    if(var.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                        mipsCode.add(indent + "l.s $f12, " + var.getBaseLabel());
                    } else {
                        mipsCode.add(indent + "l.s $f12, " + var.getStackAreaAddressLabel() + var.getOffset()+ "($sp)");
                    }
//                    mipsCode.add(indent + "l.s $f12, " + getMemoryMapping(variable) + "($sp)");
                }
            }
            mipsCode.add(indent + "li $v0 , 2");
            mipsCode.add(indent + "syscall");
            //print new line
            mipsCode.add(indent + "li $a0, 10");
            mipsCode.add(indent + "li $v0 , 11");
            mipsCode.add(indent + "syscall");
        } else if(ctx.funtionName.getText().equalsIgnoreCase("exit")) {
            IRParser.ExprListContext e = ctx.exprList();
            List<ParseTree> parseTree = e.children;
            if(parseTree != null && !parseTree.isEmpty()) {
                if(parseTree.get(0) instanceof IRParser.Expr_constantContext) {
                    String variable = ctx.exprList().getText();
                    mipsCode.add(indent + "li $a0, " + variable);
                } else {
                    String variable = ctx.exprList().getText();
                    SymbolInfo var = symbolsMap.get(variable);
                    if(var == null) var = globalSymbols.get(variable);
                    if(var.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                        mipsCode.add(indent + "lw $a0, " + var.getBaseLabel());
                    } else {
                        mipsCode.add(indent + "lw $a0, " + var.getStackAreaAddressLabel() + var.getOffset()+ "($sp)");
                    }
                }
            }

//            mipsCode.add(indent + "move $a0 , $t0"  +" # exit(i)");
            mipsCode.add(indent + "li $v0 , 17");
            mipsCode.add(indent + "syscall");
        } else {
            String functionName = ctx.funtionName.getText();
            IRParser.ExprListContext e = ctx.exprList();
            if(e != null) {
                List<ParseTree> parseTree = e.children;
                if (parseTree != null && !parseTree.isEmpty()) {
                    int paramRegIndex = 0;
                    for (ParseTree p : parseTree) {
                        if (p instanceof IRParser.Expr_constantContext) {
                            numberOfCallParams++;
                            String param;
                            boolean paramIsInt = false;
                            if (((IRParser.Expr_constantContext) p).constant().INTLIT() != null) {
                                param = ((IRParser.Expr_constantContext) p).constant().INTLIT().getText();
                                paramIsInt = true;
                            } else {
                                param = ((IRParser.Expr_constantContext) p).constant().FLOATLIT().getText();
                            }
                            if (paramIsInt) {
                                if (paramRegIndex < 4) {
                                    mipsCode.add(indent + "li $a" + (paramRegIndex) + " , " + param);
                                    mipsCode.add(indent + "sw $a" + (paramRegIndex) + " , " + 4 * paramRegIndex + "($sp)");
                                } else {
                                    mipsCode.add(indent + "li $t0, " + param);
                                    mipsCode.add(indent + "sw $t0, " + 4 * paramRegIndex + "($sp)");
                                }
                            } else {
                                if (paramRegIndex < 4) {
                                    mipsCode.add(indent + "li.s $a" + (paramRegIndex) + " , " + param);
                                    mipsCode.add(indent + "s.s $a" + (paramRegIndex) + " , " + 4 * paramRegIndex + "($sp)");
                                } else {
                                    mipsCode.add(indent + "li.s $f4, " + param);
                                    mipsCode.add(indent + "s.s $f4, " + 4 * paramRegIndex + "($sp)");
                                }
                            }
                            paramRegIndex++;
                        } else if (p instanceof IRParser.Expr_valueContext) {
                            numberOfCallParams++;
                            String param = ctx.exprList().getText();
                            SymbolInfo paramInfo = symbolsMap.get(param);
                            if (paramInfo == null) paramInfo = globalSymbols.get(param);
                            if (paramInfo.getStorage().equals(SymbolInfo.Storage.LOCAL)) {
                                if (paramInfo.getType().equalsIgnoreCase("int")) {
                                    if (paramRegIndex < 4) {
                                        mipsCode.add(indent + "lw $a" + paramRegIndex + ", " + paramInfo.getStackAreaAddressLabel() + paramInfo.getOffset() + "($sp)");
                                        mipsCode.add(indent + "sw $a" + paramRegIndex + ", " + 4 * paramRegIndex + "($sp)");
                                    } else {
                                        mipsCode.add(indent + "lw $t0, " + paramRegIndex + "($sp)");
                                        mipsCode.add(indent + "sw $t0, " + (4 * paramRegIndex) + "($sp)");
                                    }
                                } else {
                                    if (paramRegIndex < 4) {
                                        mipsCode.add(indent + "li.s $a" + paramRegIndex + ", " + paramInfo.getStackAreaAddressLabel() + paramInfo.getOffset() + "($sp)");
                                        mipsCode.add(indent + "s.s $a" + paramRegIndex + ", " + 4 * paramRegIndex + "($sp)");
                                    } else {
                                        mipsCode.add(indent + "li.s $t0, " + paramRegIndex + "($sp)");
                                        mipsCode.add(indent + "s.s $t0, " + (4 * paramRegIndex) + "($sp)");
                                    }
                                }
                            }
                            paramRegIndex++;
                        }
                    }
                }
            }
            mipsCode.add(indent + "jal " + functionName);
            if (numberOfCallParams > maxFunctionParams) {
                maxFunctionParams = numberOfCallParams;
            }
        }
    }


    @Override public void enterStat_callreturn(IRParser.Stat_callreturnContext ctx) {
        int numberOfCallParams = 0;
        String functionName = ctx.funtionName.getText();
        IRParser.ExprListContext e = ctx.exprList();
        List<ParseTree> parseTree = e.children;
        if(e != null) {
            if (parseTree != null && !parseTree.isEmpty()) {
                int paramRegIndex = 0;
                for (ParseTree p : parseTree) {
                    if (p instanceof IRParser.Expr_constantContext) {
                        numberOfCallParams++;
                        String param;
                        boolean paramIsInt = false;
                        if (((IRParser.Expr_constantContext) p).constant().INTLIT() != null) {
                            param = ((IRParser.Expr_constantContext) p).constant().INTLIT().getText();
                            paramIsInt = true;
                        } else {
                            param = ((IRParser.Expr_constantContext) p).constant().FLOATLIT().getText();
                        }
                        if (paramIsInt) {
                            if (paramRegIndex < 4) {
                                mipsCode.add(indent + "li $a" + (paramRegIndex) + " , " + param);
                                mipsCode.add(indent + "sw $a" + (paramRegIndex) + " , " + 4 * paramRegIndex + "($sp)");
                            } else {
                                mipsCode.add(indent + "li $t0, " + param);
                                mipsCode.add(indent + "sw $t0, " + 4 * paramRegIndex + "($sp)");
                            }
                        } else {
                            if (paramRegIndex < 4) {
                                mipsCode.add(indent + "li.s $a" + (paramRegIndex) + " , " + param);
                                mipsCode.add(indent + "s.s $a" + (paramRegIndex) + " , " + 4 * paramRegIndex + "($sp)");
                            } else {
                                mipsCode.add(indent + "li.s $f4, " + param);
                                mipsCode.add(indent + "s.s $f4, " + 4 * paramRegIndex + "($sp)");
                            }
                        }
                        paramRegIndex++;
                    } else if (p instanceof IRParser.Expr_valueContext) {
                        numberOfCallParams++;
                        String param = ctx.exprList().getText();
                        SymbolInfo paramInfo = symbolsMap.get(param);
                        if (paramInfo == null) paramInfo = globalSymbols.get(param);
                        if (paramInfo.getStorage().equals(SymbolInfo.Storage.LOCAL)) {
                            if (paramInfo.getType().equalsIgnoreCase("int")) {
                                if (paramRegIndex < 4) {
                                    mipsCode.add(indent + "lw $a" + paramRegIndex + ", " + paramInfo.getStackAreaAddressLabel() + paramInfo.getOffset() + "($sp)");
                                    mipsCode.add(indent + "sw $a" + paramRegIndex + ", " + 4 * paramRegIndex + "($sp)");
                                } else {
                                    mipsCode.add(indent + "lw $t0, " + paramRegIndex + "($sp)");
                                    mipsCode.add(indent + "sw $t0, " + (4 * paramRegIndex) + "($sp)");
                                }
                            } else {
                                if (paramRegIndex < 4) {
                                    mipsCode.add(indent + "li.s $a" + paramRegIndex + ", " + paramInfo.getStackAreaAddressLabel() + paramInfo.getOffset() + "($sp)");
                                    mipsCode.add(indent + "s.s $a" + paramRegIndex + ", " + 4 * paramRegIndex + "($sp)");
                                } else {
                                    mipsCode.add(indent + "li.s $t0, " + paramRegIndex + "($sp)");
                                    mipsCode.add(indent + "s.s $t0, " + (4 * paramRegIndex) + "($sp)");
                                }
                            }
                        }
                        paramRegIndex++;
                    }
                }
            }
        }
        mipsCode.add(indent + "jal " + functionName);
        if (numberOfCallParams > maxFunctionParams) {
            maxFunctionParams = numberOfCallParams;
        }
        SymbolInfo retTypeInfo = symbolsMap.get(ctx.returnValue.getText());
        if (retTypeInfo == null) retTypeInfo = globalSymbols.get(ctx.returnValue.getText());
        if (retTypeInfo.getStorage().equals(SymbolInfo.Storage.STATIC)) {
            if (retTypeInfo.getType().equalsIgnoreCase("int")) {
                mipsCode.add(indent + "sw $v0, " + retTypeInfo.getBaseLabel() + "($sp)");
            } else {
                mipsCode.add(indent + "s.s $v0, " + retTypeInfo.getBaseLabel() + "($sp)");
            }
        } else {
            if (retTypeInfo.getType().equalsIgnoreCase("int")) {
                mipsCode.add(indent + "sw $v0, " + retTypeInfo.getStackAreaAddressLabel() + retTypeInfo.getOffset() + "($sp)");
            } else {
                mipsCode.add(indent + "s.s $v0, " + retTypeInfo.getStackAreaAddressLabel() + retTypeInfo.getOffset() + "($sp)");
            }
        }
    }

    private void load(boolean isStatic, boolean isInt, boolean isConstant, String expression, int register, boolean convert) {
        if(isConstant) {
            if(convert) {
                if(isInt) {
                    mipsCode.add(indent + "li.s " + (register == 1 ? "$f4" : "$f5") + ", " + expression + ".0");
                } else {
                    mipsCode.add(indent + "li.s " + (register == 1 ? "$f4" : "$f5") + ", " + expression);
                }
            } else {
                if(isInt) {
                    mipsCode.add(indent + "li " + (register==1?"$t0":"$t1") + ", " +  expression);
                } else {
                    mipsCode.add(indent + "li.s " + (register==1?"$f4":"$f5") + ", " + expression);
                }
            }
        } else {
            if(isStatic) {
                if(convert) {
                    if (isInt) {
                        mipsCode.add(indent + "l.s " + (register == 1 ? "$f4" : "$f5") + ", " + expression);
                        mipsCode.add(indent + "cvt.s.w " + (register==1?"$f4":"$f5") + ", " + (register==1?"$f4":"$f5"));
                    } else {
                        mipsCode.add(indent + "l.s " + (register == 1 ? "$f4" : "$f5") + ", " + expression);
                    }
                } else {
                    if (isInt) {
                        mipsCode.add(indent + "lw " + (register == 1 ? "$t0" : "$t1") + ", " + expression);
                    } else {
                        mipsCode.add(indent + "l.s " + (register == 1 ? "$f4" : "$f5") + ", " + expression);
                    }
                }
            } else {
                SymbolInfo info = symbolsMap.get(expression);
                if(info == null) info = globalSymbols.get(expression);
                if(convert) {
                    if (isInt) {
                        mipsCode.add(indent + "l.s " + (register == 1 ? "$f4" : "$f5") + ", " + info.getStackAreaAddressLabel() + info.getOffset() + "($sp)");
                        mipsCode.add(indent + "cvt.s.w " + (register == 1 ? "$f4" : "$f5") + ", " + (register == 1 ? "$f4" : "$f5"));
                    } else {
                        mipsCode.add(indent + "l.s " + (register == 1 ? "$f4" : "$f5") + ", " + info.getStackAreaAddressLabel() + info.getOffset() + "($sp)");
                    }
                } else {
                    if (isInt) {
                        mipsCode.add(indent + "lw " + (register == 1 ? "$t0" : "$t1") + ", " + info.getStackAreaAddressLabel() + info.getOffset() + "($sp)");
                    } else {
                        mipsCode.add(indent + "l.s " + (register == 1 ? "$f4" : "$f5") + ", " + info.getStackAreaAddressLabel() + info.getOffset() + "($sp)");
                    }
                }
            }
        }
    }

    @Override public void enterStat_math(IRParser.Stat_mathContext ctx) {
        boolean leftOperandIsConstant = false;
        boolean leftOperandIsInt = false;
        boolean leftOperandIsStatic = false;
        List<ParseTree> parseTree = ctx.leftExp.children;
        if(parseTree != null && !parseTree.isEmpty() && parseTree.size() == 1) {
            ParseTree p = parseTree.get(0);
            if (p instanceof IRParser.ConstantContext) {
                leftOperandIsConstant = true;
                if(((IRParser.ConstantContext)p).INTLIT() != null) {
                    leftOperandIsInt = true;
                }
            } else {
                SymbolInfo left = symbolsMap.get(ctx.leftExp.getText());
                if(left == null) left = globalSymbols.get(ctx.leftExp.getText());
                if(left.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                    leftOperandIsStatic = true;
                }
                if(left.getType().equalsIgnoreCase("int")) {
                    leftOperandIsInt = true;
                }
            }
        }
        boolean rightOperandIsConstant = false;
        boolean rightOperandIsInt = false;
        boolean rightOperandIsStatic = false;

        parseTree = ctx.rightExp.children;
        if(parseTree != null && !parseTree.isEmpty() && parseTree.size() == 1) {
            ParseTree p = parseTree.get(0);
            if (p instanceof IRParser.ConstantContext) {
                rightOperandIsConstant = true;
                if(((IRParser.ConstantContext)p).INTLIT() != null) {
                    rightOperandIsInt = true;
                }
            } else {
                SymbolInfo right = symbolsMap.get(ctx.rightExp.getText());
                if(right == null) right = globalSymbols.get(ctx.rightExp.getText());
                if(right.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                    rightOperandIsStatic = true;
                }
                if(right.getType().equalsIgnoreCase("int")) {
                    rightOperandIsInt = true;
                }
            }
        }
        SymbolInfo result = symbolsMap.get(ctx.result.getText());
        if(result == null) result = globalSymbols.get(ctx.result.getText());
//        if(result.getType().equalsIgnoreCase("float") && leftOperandIsInt && rightOperandIsInt) {
//            mipsCode.add(indent + "sw $t0, " + );
//            mipsCode.add(indent + "cvt.s.w $f4, $t0");
//        }

        boolean convert = false;
        if(result.getType().equalsIgnoreCase("float") || !leftOperandIsInt || !rightOperandIsInt) {
            convert = true;
        }
        load(leftOperandIsStatic, leftOperandIsInt, leftOperandIsConstant, ctx.leftExp.getText(), 1,convert);
        load(rightOperandIsStatic, rightOperandIsInt, rightOperandIsConstant, ctx.rightExp.getText(), 2, convert);

        switch (ctx.operation().getText()) {
            case "add":
                if(leftOperandIsInt && rightOperandIsInt) {
                    mipsCode.add(indent + "add $t0, $t0, $t1");
                } else {
                    mipsCode.add(indent + "add.s $f4, $f4, $f5");
                }
                break;
            case "div":
                if(leftOperandIsInt && rightOperandIsInt) {
                    mipsCode.add(indent + "div $t0, $t0, $t1");
                } else {
                    mipsCode.add(indent + "div.s $f4, $f4, $f5");
                }
                break;
            case "mult":
                if(leftOperandIsInt && rightOperandIsInt) {
                    mipsCode.add(indent + "mul $t0, $t0, $t1");
                } else {
                    mipsCode.add(indent + "mul.s $f4, $f4, $f5");
                }
                break;
            case "sub":
                if(leftOperandIsInt && rightOperandIsInt) {
                    mipsCode.add(indent + "sub $t0, $t0, $t1");
                } else {
                    mipsCode.add(indent + "sub.s $f4, $f4, $f5");
                }
                break;
            case "and":
                if(leftOperandIsInt && rightOperandIsInt) {
                    mipsCode.add(indent + "and $t0, $t0, $t1");
                } else {
                    new RuntimeException("Not implemented");
                }
                break;
            case "or":
                if(leftOperandIsInt && rightOperandIsInt) {
                    mipsCode.add(indent + "or $t0, $t0, $t1");
                } else {
                    new RuntimeException("Not implemented");
                }
                break;
            default:
                break;
        }
        if(result.getStorage().equals(SymbolInfo.Storage.STATIC)) {
            if (result.getType().equalsIgnoreCase("int")) {
                mipsCode.add(indent + "sw $t0, " + result.getBaseLabel());
            } else {
                mipsCode.add(indent + "s.s $f4, " + result.getBaseLabel());
            }
        } else {
            if (result.getType().equalsIgnoreCase("int")) {
                mipsCode.add(indent + "sw $t0, " + result.getStackAreaAddressLabel() + ( result.getOffset()+ "($sp)"));
            } else {
                mipsCode.add(indent + "s.s $f4, " + result.getStackAreaAddressLabel() + ( result.getOffset()+ "($sp)"));
            }
        }
    }

    @Override public void enterStat_condition(IRParser.Stat_conditionContext ctx) {
        String label = ctx.label.getText();

        boolean leftExprIsConstant = false;
        List<ParseTree> parseTree = ctx.leftExp.children;
        if(parseTree != null && !parseTree.isEmpty() && parseTree.size() == 1) {
            ParseTree p = parseTree.get(0);
            if (p instanceof IRParser.ConstantContext) {
                leftExprIsConstant = true;
            }
        }
        boolean rightExprIsConstant = false;

        parseTree = ctx.rightExp.children;
        if(parseTree != null && !parseTree.isEmpty() && parseTree.size() == 1) {
            ParseTree p = parseTree.get(0);
            if (p instanceof IRParser.ConstantContext) {
                rightExprIsConstant = true;
            }
        }
        SymbolInfo left = symbolsMap.get(ctx.leftExp.getText());
        if(left == null) left = globalSymbols.get(ctx.leftExp.getText());

        SymbolInfo right = symbolsMap.get(ctx.rightExp.getText());
        if(right == null) right = globalSymbols.get(ctx.rightExp.getText());

        if (leftExprIsConstant) {
            mipsCode.add(indent + "li $t0, " + ctx.leftExp.getText());
        } else {
            if(left.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                mipsCode.add(indent + "lw $t0, " +  left.getBaseLabel());
            } else {
                mipsCode.add(indent + "lw $t0, " +  left.getStackAreaAddressLabel() + ( left.getOffset()+ "($sp)"));
            }
        }

        if (rightExprIsConstant) {
            mipsCode.add(indent + "li $t1, " + ctx.rightExp.getText());
        } else {
            if(right.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                mipsCode.add(indent + "lw $t1, " +  right.getBaseLabel());
            } else {
                mipsCode.add(indent + "lw $t1, " +  right.getStackAreaAddressLabel() + ( right.getOffset()+ "($sp)"));
            }
        }

        if(ctx.branchCondition().BRANCHEQUAL() != null) {
            mipsCode.add(indent + "beq $t0 , $t1, " + label);
        } else if(ctx.branchCondition().BRANCHGREATERTHAN() != null) {
            mipsCode.add(indent + "bgt $t0 , $t1, " + label);
        } else if(ctx.branchCondition().BRANCHGREATERTHANOREQUALTO() != null) {
            mipsCode.add(indent + "bge $t0 , $t1, " + label);
        } else if(ctx.branchCondition().BRANCHLESSTHAN() != null) {
            mipsCode.add(indent + "blt $t0 , $t1, " + label);
        } else if(ctx.branchCondition().BRANCHLESSTHANOREQUALTO() != null) {
            mipsCode.add(indent + "ble $t0 , $t1, " + label);
        } else if(ctx.branchCondition().BRANCHNOTEQUAL() != null) {
            mipsCode.add(indent + "bne $t0 , $t1, " + label);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterStat_label(IRParser.Stat_labelContext ctx) {
        mipsCode.add(indent + ctx.label.getText() + ":");
    }

    @Override public void enterStat_assign(IRParser.Stat_assignContext ctx) {
        //load source to a register
        //if dest is float and source is int - convert
        //store register to dest

        boolean resultIsInt = false;
        SymbolInfo left = symbolsMap.get(ctx.leftExp.getText());
        if (left == null) left = globalSymbols.get(ctx.leftExp.getText());
        if (left.getType().equalsIgnoreCase("int")) {
            resultIsInt = true;
        }

        boolean valueIsConstant = false;
        boolean valueIsInt = false;
        List<ParseTree> parseTree = ctx.rightExp.children;
        if (parseTree != null && !parseTree.isEmpty() && parseTree.size() == 1) {
            ParseTree p = parseTree.get(0);
            if (p instanceof IRParser.ConstantContext) {
                valueIsConstant = true;
                if (((IRParser.ConstantContext) p).INTLIT() != null) {
                    valueIsInt = true;
                }
            } else {
                SymbolInfo right = symbolsMap.get(ctx.rightExp.getText());
                if (right == null) right = globalSymbols.get(ctx.rightExp.getText());
                if (right.getType().equalsIgnoreCase("int")) {
                    valueIsInt = true;
                }
            }
        }

        //load right side into a register
        String register = "";
        if (valueIsConstant) {
            if (valueIsInt && resultIsInt) {
                mipsCode.add(indent + "li $t0, " + ctx.rightExp.getText());
            } else if(!valueIsInt && !resultIsInt) {
                mipsCode.add(indent + "li.s $f4, " + ctx.rightExp.getText());
            } else if(valueIsInt && !resultIsInt) { //convert
                mipsCode.add(indent + "li.s $f4, " + ctx.rightExp.getText() + ".0");
            }
        } else {
            SymbolInfo right = symbolsMap.get(ctx.rightExp.getText());
            if (right == null) right = globalSymbols.get(ctx.rightExp.getText());
            if (right.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                if (valueIsInt && resultIsInt) {
                    mipsCode.add(indent + "lw $t0, " + right.getBaseLabel());
                } else if(!valueIsInt && !resultIsInt) {
                    mipsCode.add(indent + "l.s $f4, " + right.getBaseLabel());
                } else if(valueIsInt && !resultIsInt) { //convert
                    mipsCode.add(indent + "l.s $f4, " + right.getBaseLabel());
                    mipsCode.add(indent + "cvt.s.w $f4, $f4");
                }
            } else {
                if (valueIsInt && resultIsInt) {
                    mipsCode.add(indent + "lw $t0, " + right.getStackAreaAddressLabel() + (right.getOffset() + "($sp)"));
                } else if(!valueIsInt && !resultIsInt) {
                    mipsCode.add(indent + "l.s $f4, " + right.getStackAreaAddressLabel() + (right.getOffset() + "($sp)"));
                } else if(valueIsInt && !resultIsInt) { //convert
                    mipsCode.add(indent + "l.s $f4, " + right.getStackAreaAddressLabel() + (right.getOffset() + "($sp)"));
                    mipsCode.add(indent + "cvt.s.w $f4, $f4");
                }
            }
        }

        //store register to destination
        if(left.getStorage().equals(SymbolInfo.Storage.STATIC)) {
            if (resultIsInt && valueIsInt) {
                mipsCode.add(indent + "sw  $t0, " + left.getBaseLabel());
            } else if (!resultIsInt && !valueIsInt) {
                mipsCode.add(indent + "s.s  $f4, " + left.getBaseLabel());
            } else if (!resultIsInt && valueIsInt) {
                mipsCode.add(indent + "s.s  $f4, " + left.getBaseLabel());
            } else if (resultIsInt && !valueIsInt) {
                throw new RuntimeException("Invalid assignment");
            }
        } else {
            if (resultIsInt && valueIsInt) {
                mipsCode.add(indent + "sw  $t0, " + left.getStackAreaAddressLabel() + left.getOffset() + "($sp)");
            } else if (!resultIsInt && !valueIsInt) {
                mipsCode.add(indent + "s.s  $f4, " + left.getStackAreaAddressLabel() + left.getOffset() + "($sp)");
            } else if (!resultIsInt && valueIsInt) {
                mipsCode.add(indent + "s.s  $f4, " + left.getStackAreaAddressLabel() + left.getOffset() + "($sp)");
            } else if (resultIsInt && !valueIsInt) {
                throw new RuntimeException("Invalid assignment");
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterStat_goto(IRParser.Stat_gotoContext ctx) {
        mipsCode.add(indent + "j " + ctx.label.getText());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterStat_return(IRParser.Stat_returnContext ctx) {
        boolean isReturnValueInt = false;
        boolean isReturnValueConstant = false;
        if(ctx.expr() != null) {
            List<ParseTree> parseTree = ctx.expr().children;
            if (parseTree != null && !parseTree.isEmpty() && parseTree.size() == 1) {
                ParseTree p = parseTree.get(0);
                if (p instanceof IRParser.ConstantContext) {
                    isReturnValueConstant = true;
                    if (((IRParser.ConstantContext) p).INTLIT() != null) {
                        isReturnValueInt = true;
                        if(returnType.equalsIgnoreCase("int")) {
                            mipsCode.add(indent + "li $v0 , " + ((IRParser.ConstantContext) p).INTLIT().getText()  + " # return integer");
                        } else if(returnType.equalsIgnoreCase("float")) {
                            mipsCode.add(indent + "li.s $f4 , " + ((IRParser.ConstantContext) p).INTLIT().getText() + ".0 # return integer after coverting to float");
                            mipsCode.add(indent + "cvt.s.w $f4, $f4");
                            mipsCode.add(indent + "mov.s $v0, $f4");
                        } else {
                            throw new RuntimeException("Return not allowed for void function type");
                        }
                    } else if (((IRParser.ConstantContext) p).FLOATLIT() != null) {
                        isReturnValueInt = false;
                        if(returnType.equalsIgnoreCase("float")) {
                            mipsCode.add(indent + "li.s $f4 , " + ((IRParser.ConstantContext) p).FLOATLIT().getText()  + " # return float");
                            mipsCode.add(indent + "mov.s $v0, $f4");
                        } else if(returnType.equalsIgnoreCase("int")) {
                            throw new RuntimeException("Cannot return float when return type is int");
                        } else {
                            throw new RuntimeException("Return not allowed for void function type");
                        }
                    }
                } else {
                    SymbolInfo expr = symbolsMap.get(ctx.expr().getText());
                    if (expr == null) expr = globalSymbols.get(ctx.expr().getText());
                    if (expr.getType().equalsIgnoreCase("int")) {
                        isReturnValueInt = true;
                        if(returnType.equalsIgnoreCase("int")) {
                            if(expr.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                                mipsCode.add(indent + "lw $v0 , " + expr.getBaseLabel());
                            } else {
                                mipsCode.add(indent + "lw $v0 , " + expr.getStackAreaAddressLabel() + expr.getOffset()  + "($sp) # return integer");
                            }
                        } else if (expr.getType().equalsIgnoreCase("float")) {
                            if(expr.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                                mipsCode.add(indent + "l.s $f4 , " + expr.getBaseLabel());
                            } else {
                                mipsCode.add(indent + "l.s $f4 , " + expr.getStackAreaAddressLabel() + expr.getOffset()  + "($sp) # return integer");
                            }
                            mipsCode.add(indent + "cvt.s.w $f4, $f4");
                            mipsCode.add(indent + "mov.s $v0, $f4");
                        } else {
                            throw new RuntimeException("Return not allowed for void function type");
                        }
                    } else if (expr.getType().equalsIgnoreCase("float")) {
                        if(returnType.equalsIgnoreCase("float")) {
                            if(expr.getStorage().equals(SymbolInfo.Storage.STATIC)) {
                                mipsCode.add(indent + "l.s $f4 , " + expr.getBaseLabel());
                            } else {
                                mipsCode.add(indent + "l.s $f4 , " + expr.getStackAreaAddressLabel() + expr.getOffset()  + "($sp) # return integer");
                            }
                            mipsCode.add(indent + "mov.s $v0, $f4");
                        } else if (expr.getType().equalsIgnoreCase("int")) {
                            throw new RuntimeException("Cannot return float when return type is int");
                        } else {
                            throw new RuntimeException("Return not allowed for void function type");
                        }
                    }
                }
            }
        } else {
            mipsCode.add(indent + "li $v0 , 0 # return 0");
        }
    }


    int registerSuffix;
    private String getAvailableRegister() {
        return registerSuffix + "";
    }

    @Override public void enterBranchCondition(IRParser.BranchConditionContext ctx) {
    }
}

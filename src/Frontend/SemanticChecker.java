package Frontend;

import AST.*;
import Util.error.semanticError;
import Util.symbol.*;

public class SemanticChecker implements ASTVisitor {
    public Scope globalScope, currentScope;
    public Type currentReturnType;
    public classType currentClass;
    public boolean returnDone;
    public int loopDeep = 0;

    public SemanticChecker(Scope global) {
        this.globalScope = global;
    }

    @Override
    public void visit(ProgramNode it) {
        funcSymbol main = globalScope.getFunction("main", false, it.pos);
        if (!main.returnType.isInt()) throw new semanticError("main function must return int", it.pos);
        if (main.paramList.size() != 0) throw new semanticError("main function should not have parameters", it.pos);
        currentScope = globalScope;
        it.body.forEach(x -> x.accept(this));
    }

    @Override
    public void visit(TypeNode it) {

    }

    @Override
    public void visit(funcDef it) {
        if (it.type != null) currentReturnType = globalScope.getType(it.type);
        else currentReturnType = new primitiveType("void");
        returnDone = false;
        currentScope = new Scope(currentScope);
        it.paramList.forEach(x -> currentScope.defineVariable(x.name, new varSymbol(x.name, globalScope.getType(x.type)), x.pos));
        it.block.accept(this);
        currentScope = currentScope.parentScope;
        if (it.name.equals("main")) returnDone = true;
        if (it.type != null && !it.type.simpleType.equals("void") && !returnDone)
            throw new semanticError("No return", it.pos);
    }

    @Override
    public void visit(classDef it) {
        currentClass = (classType) globalScope.typeMap.get(it.name);
        currentScope = new Scope(currentScope);
        currentClass.varMap.forEach((key, value) -> currentScope.defineVariable(key, value, it.pos));
        currentClass.funcMap.forEach((key, value) -> currentScope.defineFunction(key, value, it.pos));
        //it.varList.forEach(x->x.accept(this));
        it.funcList.forEach(x -> x.accept(this));
        if (it.constructor != null) {
            if (!it.constructor.name.equals(it.name)) throw new semanticError("mismatched constructor name", it.pos);
            it.constructor.accept(this);
        }
        currentScope = currentScope.parentScope;
        currentClass = null;
    }

    @Override
    public void visit(blockStmt it) {
        it.stmtList.forEach(x -> {
            if (x instanceof blockStmt) {
                currentScope = new Scope(currentScope);
                x.accept(this);
                currentScope = currentScope.parentScope;
            } else {
                x.accept(this);
            }
        });
    }

    @Override
    public void visit(varDefStmt it) {
        it.varList.forEach(x -> x.accept(this));
    }

    @Override
    public void visit(varDefSubStmt it) {
        Type varType = globalScope.getType(it.type);
        if (varType.isVoid()) throw new semanticError("void variable", it.pos);
        if (it.expr != null) {
            it.expr.accept(this);
            if (!it.expr.type.equals(varType)) throw new semanticError("variable init fail", it.pos);
        }
        currentScope.defineVariable(it.name, new varSymbol(it.name, varType), it.pos);
    }

    @Override
    public void visit(ifStmt it) {
        it.condition.accept(this);
        if (!it.condition.type.isBool()) throw new semanticError("if condition not bool", it.pos);
        currentScope = new Scope(currentScope);
        it.trueStmt.accept(this);
        currentScope = currentScope.parentScope;
        if (it.falseStmt != null) {
            currentScope = new Scope(currentScope);
            it.falseStmt.accept(this);
            currentScope = currentScope.parentScope;
        }
    }

    @Override
    public void visit(whileStmt it) {
        it.condition.accept(this);
        if (!it.condition.type.isBool()) throw new semanticError("while condition not bool", it.pos);
        loopDeep++;
        currentScope = new Scope(currentScope);
        it.body.accept(this);
        currentScope = currentScope.parentScope;
        loopDeep--;
    }

    @Override
    public void visit(forStmt it) {
        if (it.init != null) it.init.accept(this);
        if (it.condition != null) it.condition.accept(this);
        if (it.condition != null && !it.condition.type.isBool())
            throw new semanticError("while condition not bool", it.pos);
        if (it.inc != null) it.inc.accept(this);
        loopDeep++;
        currentScope = new Scope(currentScope);
        it.body.accept(this);
        currentScope = currentScope.parentScope;
        loopDeep--;
    }

    @Override
    public void visit(breakStmt it) {
        if (loopDeep == 0) throw new semanticError("break not in loop", it.pos);
    }

    @Override
    public void visit(continueStmt it) {
        if (loopDeep == 0) throw new semanticError("continue not in loop", it.pos);
    }

    @Override
    public void visit(returnStmt it) {
        returnDone = true;
        if (it.returnValue != null) {
            it.returnValue.accept(this);
            if (!it.returnValue.type.equals(currentReturnType)) throw new semanticError("return type error", it.pos);
        } else {
            if (!currentReturnType.isVoid()) throw new semanticError("return type error", it.pos);
        }
    }

    @Override
    public void visit(pureExprStmt it) {
        it.expr.accept(this);
    }

    @Override
    public void visit(emptyStmt it) {

    }

    @Override
    public void visit(varExpr it) {
        it.type = currentScope.getVariable(it.name, true, it.pos).type;
    }

    @Override
    public void visit(subscriptExpr it) {
        it.base.accept(this);
        it.offset.accept(this);
        if (!(it.base.type instanceof arrayType)) throw new semanticError("not an array", it.pos);
        if (!it.offset.type.isInt()) throw new semanticError("subscript not int", it.pos);
        arrayType arrayType = (arrayType) it.base.type;
        if (arrayType.dim - 1 == 0) it.type = arrayType.simpleType;
        else it.type = new arrayType(arrayType.simpleType, arrayType.dim - 1);
    }

    @Override
    public void visit(funcCallExpr it) {
        if (it.base instanceof varExpr) {
            it.base.type = currentScope.getFunction(((varExpr) it.base).name, true, it.pos);
        } else it.base.accept(this);
        if (!(it.base.type instanceof funcSymbol)) throw new semanticError("not a function", it.pos);
        funcSymbol func = (funcSymbol) it.base.type;
        it.exprList.forEach(x -> x.accept(this));
        if (func.paramList.size() != it.exprList.size()) throw new semanticError("parameter size error", it.pos);
        for (int i = 0; i < func.paramList.size(); i++) {
            if (!func.paramList.get(i).type.equals(it.exprList.get(i).type))
                throw new semanticError("parameter type error", it.pos);
        }
        it.type = func.returnType;
    }

    @Override
    public void visit(exprListExpr it) {

    }

    @Override
    public void visit(memberExpr it) {
        it.base.accept(this);
        if (it.base.type instanceof arrayType && it.isFunc && it.name.equals("size")) {
            funcSymbol func = new funcSymbol("size");
            func.returnType = new primitiveType("int");
            it.type = func;
            return;
        }
        if (it.base.type.isString() && it.isFunc && it.name.equals("length")) {
            funcSymbol func = new funcSymbol("length");
            func.returnType = new primitiveType("int");
            it.type = func;
            return;
        }
        if (it.base.type.isString() && it.isFunc && it.name.equals("substring")) {
            funcSymbol func = new funcSymbol("substring");
            func.returnType = new primitiveType("string");
            func.paramList.add(new varSymbol("left", new primitiveType("int")));
            func.paramList.add(new varSymbol("right", new primitiveType("int")));
            it.type = func;
            return;
        }
        if (it.base.type.isString() && it.isFunc && it.name.equals("parseInt")) {
            funcSymbol func = new funcSymbol("parseInt");
            func.returnType = new primitiveType("int");
            it.type = func;
            return;
        }
        if (it.base.type.isString() && it.isFunc && it.name.equals("ord")) {
            funcSymbol func = new funcSymbol("ord");
            func.returnType = new primitiveType("int");
            func.paramList.add(new varSymbol("pos", new primitiveType("int")));
            it.type = func;
            return;
        }
        if (!(it.base.type instanceof classType)) throw new semanticError("not a class", it.pos);
        classType classType = (classType) it.base.type;
        if (it.isFunc) {
            if (classType.funcMap.containsKey(it.name)) it.type = classType.funcMap.get(it.name);
            else throw new semanticError("such symbol", it.pos);
        } else {
            if (classType.varMap.containsKey(it.name)) it.type = classType.varMap.get(it.name).type;
            else throw new semanticError("such symbol", it.pos);
        }
    }

    @Override
    public void visit(newExpr it) {
        if (it.exprList != null) {
            it.exprList.forEach(x -> {
                x.accept(this);
                if (!x.type.isInt()) throw new semanticError("not int", x.pos);
            });
        }
        it.type = globalScope.getType(it.typeNode);
    }

    @Override
    public void visit(suffixExpr it) {
        it.src.accept(this);
        if (!it.src.type.isInt()) throw new semanticError("not int", it.pos);
        if (!it.src.assignable) throw new semanticError("not assignable", it.pos);
        it.type = it.src.type;
    }

    @Override
    public void visit(prefixExpr it) {
        it.src.accept(this);
        switch (it.op) {
            case "++":
            case "--":
                if (!it.src.assignable) throw new semanticError("not assignable", it.pos);
                it.assignable = true;
            case "+":
            case "-":
            case "~":
                if (!it.src.type.isInt()) throw new semanticError("not int", it.pos);
                break;
            case "!":
                if (!it.src.type.isBool()) throw new semanticError("not bool", it.pos);
                break;
            default:
                break;
        }
        it.type = it.src.type;
    }

    @Override
    public void visit(binaryExpr it) {
        it.src1.accept(this);
        it.src2.accept(this);
        switch (it.op) {
            case "*":
            case "/":
            case "%":
            case "-":
            case "<<":
            case ">>":
            case "&":
            case "^":
            case "|":
                if (!(it.src1.type.isInt() && it.src2.type.isInt())) throw new semanticError("not int", it.pos);
                it.type = new primitiveType("int");
                break;
            case "+":
                if (!((it.src1.type.isInt() && it.src2.type.isInt()) || (it.src1.type.isString() && it.src2.type.isString())))
                    throw new semanticError("not int or string", it.pos);
                it.type = it.src1.type;
                break;
            case "<":
            case ">":
            case "<=":
            case ">=":
                if (!((it.src1.type.isInt() && it.src2.type.isInt()) || (it.src1.type.isString() && it.src2.type.isString())))
                    throw new semanticError("not int or string", it.pos);
                it.type = new primitiveType("bool");
                break;
            case "&&":
            case "||":
                if (!(it.src1.type.isBool() && it.src2.type.isBool())) throw new semanticError("not bool", it.pos);
                it.type = new primitiveType("bool");
                break;
            case "==":
            case "!=":
                if (!it.src1.type.equals(it.src2.type)) throw new semanticError("not same type", it.pos);
                it.type = new primitiveType("bool");
                break;
            case "=":
                if (!it.src1.type.equals(it.src2.type)) throw new semanticError("not same type", it.pos);
                if (!it.src1.assignable) throw new semanticError("not assignable", it.pos);
                it.type = it.src1.type;
                it.assignable = true;
                break;
            default:
                break;
        }
    }

    @Override
    public void visit(thisExpr it) {
        if (currentClass != null) it.type = currentClass;
        else throw new semanticError("this not in class", it.pos);
    }

    @Override
    public void visit(intLiteralExpr it) {
        it.type = new primitiveType("int");
    }

    @Override
    public void visit(boolLiteralExpr it) {
        it.type = new primitiveType("bool");
    }

    @Override
    public void visit(stringLiteralExpr it) {
        it.type = new primitiveType("string");
    }

    @Override
    public void visit(nullLiteralExpr it) {
        it.type = new primitiveType("null");
    }
}

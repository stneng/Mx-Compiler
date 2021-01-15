package Frontend;

import AST.*;
import Util.symbol.*;

public class SymbolCollector implements ASTVisitor {
    Scope global, current;

    public SymbolCollector(Scope global) {
        this.global = global;
        this.global.typeMap.put("int", new primitiveType("int"));
        this.global.typeMap.put("bool", new primitiveType("bool"));
        this.global.typeMap.put("string", new primitiveType("string"));
        this.global.typeMap.put("void", new primitiveType("void"));
        this.global.typeMap.put("null", new primitiveType("null"));
        {
            funcSymbol func = new funcSymbol("print");
            func.returnType = new primitiveType("void");
            func.paramList.add(new varSymbol("str", new primitiveType("string")));
            this.global.funcMap.put("print", func);
        }
        {
            funcSymbol func = new funcSymbol("println");
            func.returnType = new primitiveType("void");
            func.paramList.add(new varSymbol("str", new primitiveType("string")));
            this.global.funcMap.put("println", func);
        }
        {
            funcSymbol func = new funcSymbol("printInt");
            func.returnType = new primitiveType("void");
            func.paramList.add(new varSymbol("n", new primitiveType("int")));
            this.global.funcMap.put("printInt", func);
        }
        {
            funcSymbol func = new funcSymbol("printlnInt");
            func.returnType = new primitiveType("void");
            func.paramList.add(new varSymbol("n", new primitiveType("int")));
            this.global.funcMap.put("printlnInt", func);
        }
        {
            funcSymbol func = new funcSymbol("getString");
            func.returnType = new primitiveType("string");
            this.global.funcMap.put("getString", func);
        }
        {
            funcSymbol func = new funcSymbol("getInt");
            func.returnType = new primitiveType("int");
            this.global.funcMap.put("getInt", func);
        }
        {
            funcSymbol func = new funcSymbol("toString");
            func.returnType = new primitiveType("string");
            func.paramList.add(new varSymbol("i", new primitiveType("int")));
            this.global.funcMap.put("toString", func);
        }
    }

    @Override
    public void visit(ProgramNode it) {
        current = global;
        it.body.forEach(x -> x.accept(this));
    }

    @Override
    public void visit(TypeNode it) {

    }

    @Override
    public void visit(funcDef it) {
        current.defineFunction(it.name, new funcSymbol(it.name), it.pos);
    }

    @Override
    public void visit(classDef it) {
        current = new Scope(current);
        classType a = new classType(it.name);
        it.varList.forEach(x -> x.accept(this));
        it.funcList.forEach(x -> x.accept(this));
        if (it.constructor != null) a.constructor = new funcSymbol(it.constructor.name);
        a.varMap = current.varMap;
        a.funcMap = current.funcMap;
        current = current.parentScope;
        current.defineType(it.name, a, it.pos);
    }

    @Override
    public void visit(blockStmt it) {

    }

    @Override
    public void visit(varDefStmt it) {

    }

    @Override
    public void visit(varDefSubStmt it) {
        current.defineVariable(it.name, new varSymbol(it.name), it.pos);
    }

    @Override
    public void visit(ifStmt it) {

    }

    @Override
    public void visit(whileStmt it) {

    }

    @Override
    public void visit(forStmt it) {

    }

    @Override
    public void visit(breakStmt it) {

    }

    @Override
    public void visit(continueStmt it) {

    }

    @Override
    public void visit(returnStmt it) {

    }

    @Override
    public void visit(pureExprStmt it) {

    }

    @Override
    public void visit(emptyStmt it) {

    }

    @Override
    public void visit(varExpr it) {

    }

    @Override
    public void visit(subscriptExpr it) {

    }

    @Override
    public void visit(funcCallExpr it) {

    }

    @Override
    public void visit(exprListExpr it) {

    }

    @Override
    public void visit(memberExpr it) {

    }

    @Override
    public void visit(newExpr it) {

    }

    @Override
    public void visit(suffixExpr it) {

    }

    @Override
    public void visit(prefixExpr it) {

    }

    @Override
    public void visit(binaryExpr it) {

    }

    @Override
    public void visit(thisExpr it) {

    }

    @Override
    public void visit(intLiteralExpr it) {

    }

    @Override
    public void visit(boolLiteralExpr it) {

    }

    @Override
    public void visit(stringLiteralExpr it) {

    }

    @Override
    public void visit(nullLiteralExpr it) {

    }
}

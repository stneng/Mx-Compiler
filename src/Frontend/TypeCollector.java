package Frontend;

import AST.*;
import Util.symbol.Scope;
import Util.symbol.classType;
import Util.symbol.varSymbol;

public class TypeCollector implements ASTVisitor {
    Scope global;
    String currentClass;

    public TypeCollector(Scope global) {
        this.global = global;
    }

    @Override
    public void visit(ProgramNode it) {
        currentClass = null;
        it.body.forEach(x -> x.accept(this));
    }

    @Override
    public void visit(TypeNode it) {

    }

    @Override
    public void visit(funcDef it) {
        if (currentClass == null) {
            global.funcMap.get(it.name).returnType = global.getType(it.type);
            it.paramList.forEach(x -> global.funcMap.get(it.name).paramList.add(new varSymbol(x.name, global.getType(x.type))));
        } else {
            ((classType) global.typeMap.get(currentClass)).funcMap.get(it.name).returnType = global.getType(it.type);
            it.paramList.forEach(x -> ((classType) global.typeMap.get(currentClass)).funcMap.get(it.name).paramList.add(new varSymbol(x.name, global.getType(x.type))));
        }
    }

    @Override
    public void visit(classDef it) {
        currentClass = it.name;
        it.varList.forEach(x -> x.accept(this));
        it.funcList.forEach(x -> x.accept(this));
        if (it.constructor != null) ((classType) global.typeMap.get(currentClass)).constructor.returnType = null;
        currentClass = null;
    }

    @Override
    public void visit(blockStmt it) {

    }

    @Override
    public void visit(varDefStmt it) {

    }

    @Override
    public void visit(varDefSubStmt it) {
        if (currentClass == null) global.varMap.get(it.name).type = global.getType(it.type);
        else ((classType) global.typeMap.get(currentClass)).varMap.get(it.name).type = global.getType(it.type);
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

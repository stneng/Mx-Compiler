package AST;

public interface ASTVisitor {
    void visit(ProgramNode it);

    void visit(TypeNode it);

    void visit(funcDef it);

    void visit(classDef it);

    void visit(blockStmt it);

    void visit(varDefStmt it);

    void visit(varDefSubStmt it);

    void visit(ifStmt it);

    void visit(whileStmt it);

    void visit(forStmt it);

    void visit(breakStmt it);

    void visit(continueStmt it);

    void visit(returnStmt it);

    void visit(pureExprStmt it);

    void visit(emptyStmt it);

    void visit(varExpr it);

    void visit(subscriptExpr it);

    void visit(funcCallExpr it);

    void visit(exprListExpr it);

    void visit(memberExpr it);

    void visit(newExpr it);

    void visit(suffixExpr it);

    void visit(prefixExpr it);

    void visit(binaryExpr it);

    void visit(thisExpr it);

    void visit(intLiteralExpr it);

    void visit(boolLiteralExpr it);

    void visit(stringLiteralExpr it);

    void visit(nullLiteralExpr it);
}

// Generated from Mx.g4 by ANTLR 4.9.1
package Parser;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MxParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 *            operations with no return type.
 */
public interface MxVisitor<T> extends ParseTreeVisitor<T> {
    /**
     * Visit a parse tree produced by {@link MxParser#program}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitProgram(MxParser.ProgramContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#programSub}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitProgramSub(MxParser.ProgramSubContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#varDef}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitVarDef(MxParser.VarDefContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#varDefSub}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitVarDefSub(MxParser.VarDefSubContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#funcDef}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFuncDef(MxParser.FuncDefContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#classDef}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitClassDef(MxParser.ClassDefContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#paramList}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitParamList(MxParser.ParamListContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#param}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitParam(MxParser.ParamContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#type}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitType(MxParser.TypeContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#simpleType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSimpleType(MxParser.SimpleTypeContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#returnType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitReturnType(MxParser.ReturnTypeContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#block}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBlock(MxParser.BlockContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#primary}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPrimary(MxParser.PrimaryContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#literal}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitLiteral(MxParser.LiteralContext ctx);

    /**
     * Visit a parse tree produced by the {@code errorCreator}
     * labeled alternative in {@link MxParser#creator}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitErrorCreator(MxParser.ErrorCreatorContext ctx);

    /**
     * Visit a parse tree produced by the {@code arrayCreator}
     * labeled alternative in {@link MxParser#creator}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitArrayCreator(MxParser.ArrayCreatorContext ctx);

    /**
     * Visit a parse tree produced by the {@code classCreator}
     * labeled alternative in {@link MxParser#creator}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitClassCreator(MxParser.ClassCreatorContext ctx);

    /**
     * Visit a parse tree produced by the {@code simpleCreator}
     * labeled alternative in {@link MxParser#creator}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSimpleCreator(MxParser.SimpleCreatorContext ctx);

    /**
     * Visit a parse tree produced by the {@code blockStmt}
     * labeled alternative in {@link MxParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBlockStmt(MxParser.BlockStmtContext ctx);

    /**
     * Visit a parse tree produced by the {@code varDefStmt}
     * labeled alternative in {@link MxParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitVarDefStmt(MxParser.VarDefStmtContext ctx);

    /**
     * Visit a parse tree produced by the {@code ifStmt}
     * labeled alternative in {@link MxParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitIfStmt(MxParser.IfStmtContext ctx);

    /**
     * Visit a parse tree produced by the {@code whileStmt}
     * labeled alternative in {@link MxParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitWhileStmt(MxParser.WhileStmtContext ctx);

    /**
     * Visit a parse tree produced by the {@code forStmt}
     * labeled alternative in {@link MxParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitForStmt(MxParser.ForStmtContext ctx);

    /**
     * Visit a parse tree produced by the {@code breakStmt}
     * labeled alternative in {@link MxParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBreakStmt(MxParser.BreakStmtContext ctx);

    /**
     * Visit a parse tree produced by the {@code continueStmt}
     * labeled alternative in {@link MxParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitContinueStmt(MxParser.ContinueStmtContext ctx);

    /**
     * Visit a parse tree produced by the {@code returnStmt}
     * labeled alternative in {@link MxParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitReturnStmt(MxParser.ReturnStmtContext ctx);

    /**
     * Visit a parse tree produced by the {@code pureExprStmt}
     * labeled alternative in {@link MxParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPureExprStmt(MxParser.PureExprStmtContext ctx);

    /**
     * Visit a parse tree produced by the {@code emptyStmt}
     * labeled alternative in {@link MxParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitEmptyStmt(MxParser.EmptyStmtContext ctx);

    /**
     * Visit a parse tree produced by the {@code newExpr}
     * labeled alternative in {@link MxParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNewExpr(MxParser.NewExprContext ctx);

    /**
     * Visit a parse tree produced by the {@code prefixExpr}
     * labeled alternative in {@link MxParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPrefixExpr(MxParser.PrefixExprContext ctx);

    /**
     * Visit a parse tree produced by the {@code subscriptExpr}
     * labeled alternative in {@link MxParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSubscriptExpr(MxParser.SubscriptExprContext ctx);

    /**
     * Visit a parse tree produced by the {@code memberExpr}
     * labeled alternative in {@link MxParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitMemberExpr(MxParser.MemberExprContext ctx);

    /**
     * Visit a parse tree produced by the {@code suffixExpr}
     * labeled alternative in {@link MxParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSuffixExpr(MxParser.SuffixExprContext ctx);

    /**
     * Visit a parse tree produced by the {@code atomExpr}
     * labeled alternative in {@link MxParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAtomExpr(MxParser.AtomExprContext ctx);

    /**
     * Visit a parse tree produced by the {@code binaryExpr}
     * labeled alternative in {@link MxParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBinaryExpr(MxParser.BinaryExprContext ctx);

    /**
     * Visit a parse tree produced by the {@code funcCallExpr}
     * labeled alternative in {@link MxParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFuncCallExpr(MxParser.FuncCallExprContext ctx);

    /**
     * Visit a parse tree produced by {@link MxParser#expressionList}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitExpressionList(MxParser.ExpressionListContext ctx);
}

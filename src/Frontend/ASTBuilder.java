package Frontend;

import AST.*;
import Parser.MxBaseVisitor;
import Parser.MxParser;
import Util.error.syntaxError;
import Util.position;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;

public class ASTBuilder extends MxBaseVisitor<ASTNode> {
    @Override
    public ASTNode visitProgram(MxParser.ProgramContext ctx) {
        ProgramNode ans = new ProgramNode(new position(ctx));
        if (ctx.programSub() != null) {
            for (ParserRuleContext x : ctx.programSub()) {
                ASTNode t = visit(x);
                if (t instanceof varDefStmt) {
                    ans.body.addAll(((varDefStmt) t).varList);
                } else {
                    ans.body.add(t);
                }
            }
        }
        return ans;
    }

    @Override
    public ASTNode visitProgramSub(MxParser.ProgramSubContext ctx) {
        if (ctx.funcDef() != null) return visit(ctx.funcDef());
        else if (ctx.varDef() != null) return visit(ctx.varDef());
        else return visit(ctx.classDef());
    }

    @Override
    public ASTNode visitVarDef(MxParser.VarDefContext ctx) {
        varDefStmt ans = new varDefStmt(new position(ctx));
        TypeNode type = (TypeNode) visit(ctx.type());
        for (ParserRuleContext x : ctx.varDefSub()) {
            varDefSubStmt t = (varDefSubStmt) visit(x);
            t.type = type;
            ans.varList.add(t);
        }
        return ans;
    }

    @Override
    public ASTNode visitVarDefSub(MxParser.VarDefSubContext ctx) {
        return new varDefSubStmt(new position(ctx), ctx.Identifier().getText(), ctx.expression() != null ? (ExprNode) visit(ctx.expression()) : null);
    }

    @Override
    public ASTNode visitFuncDef(MxParser.FuncDefContext ctx) {
        return new funcDef(new position(ctx), ctx.Identifier().getText(), ctx.returnType() != null ? (TypeNode) visit(ctx.returnType()) : null, (blockStmt) visit(ctx.block()), ctx.paramList() != null ? ((varDefStmt) visit(ctx.paramList())).varList : new ArrayList<>());
    }

    @Override
    public ASTNode visitClassDef(MxParser.ClassDefContext ctx) {
        classDef ans = new classDef(new position(ctx), ctx.Identifier().getText());
        if (ctx.varDef() != null) {
            for (ParserRuleContext x : ctx.varDef()) {
                varDefStmt t = (varDefStmt) visit(x);
                ans.varList.addAll(t.varList);
            }
        }
        if (ctx.funcDef() != null) {
            for (ParserRuleContext x : ctx.funcDef()) {
                funcDef t = (funcDef) visit(x);
                if (t.type == null) ans.constructor = t;
                else ans.funcList.add(t);
            }
        }
        return ans;
    }

    @Override
    public ASTNode visitParamList(MxParser.ParamListContext ctx) {
        varDefStmt ans = new varDefStmt(new position(ctx));
        for (ParserRuleContext x : ctx.param()) {
            varDefSubStmt t = (varDefSubStmt) visit(x);
            ans.varList.add(t);
        }
        return ans;
    }

    @Override
    public ASTNode visitParam(MxParser.ParamContext ctx) {
        varDefSubStmt ans = new varDefSubStmt(new position(ctx), ctx.Identifier().getText(), null);
        ans.type = (TypeNode) visit(ctx.type());
        return ans;
    }

    @Override
    public ASTNode visitType(MxParser.TypeContext ctx) {
        return new TypeNode(new position(ctx), ctx.simpleType().getText(), (ctx.getChildCount() - 1) / 2);
    }

    @Override
    public ASTNode visitSimpleType(MxParser.SimpleTypeContext ctx) {
        return new TypeNode(new position(ctx), ctx.getText(), 0);
    }

    @Override
    public ASTNode visitReturnType(MxParser.ReturnTypeContext ctx) {
        if (ctx.type() != null) return visit(ctx.type());
        else return new TypeNode(new position(ctx), ctx.Void().getText(), 0);
    }

    @Override
    public ASTNode visitBlock(MxParser.BlockContext ctx) {
        blockStmt ans = new blockStmt(new position(ctx));
        if (ctx.statement() != null) {
            for (ParserRuleContext x : ctx.statement()) {
                StmtNode t = (StmtNode) visit(x);
                ans.stmtList.add(t);
            }
        }
        return ans;
    }

    @Override
    public ASTNode visitPrimary(MxParser.PrimaryContext ctx) {
        if (ctx.expression() != null) return visit(ctx.expression());
        else if (ctx.This() != null) return new thisExpr(new position(ctx));
        else if (ctx.Identifier() != null) return new varExpr(new position(ctx), ctx.Identifier().getText());
        else return visit(ctx.literal());
    }

    @Override
    public ASTNode visitLiteral(MxParser.LiteralContext ctx) {
        if (ctx.IntLiteral() != null)
            return new intLiteralExpr(new position(ctx), Integer.parseInt(ctx.IntLiteral().getText()));
        else if (ctx.BoolLiteral() != null)
            return new boolLiteralExpr(new position(ctx), Boolean.parseBoolean(ctx.BoolLiteral().getText()));
        else if (ctx.StrLiteral() != null) return new stringLiteralExpr(new position(ctx), ctx.StrLiteral().getText());
        else return new nullLiteralExpr(new position(ctx));
    }

    @Override
    public ASTNode visitErrorCreator(MxParser.ErrorCreatorContext ctx) {
        throw new syntaxError("ErrorCreator", new position(ctx));
    }

    @Override
    public ASTNode visitArrayCreator(MxParser.ArrayCreatorContext ctx) {
        ArrayList<ExprNode> exprList = new ArrayList<>();
        for (ParserRuleContext x : ctx.expression()) {
            exprList.add((ExprNode) visit(x));
        }
        return new newExpr(new position(ctx), (TypeNode) visit(ctx.simpleType()), (ctx.getChildCount() - ctx.expression().size() - 1) / 2, exprList);
    }

    @Override
    public ASTNode visitClassCreator(MxParser.ClassCreatorContext ctx) {
        return new newExpr(new position(ctx), (TypeNode) visit(ctx.simpleType()), 0, null);
    }

    @Override
    public ASTNode visitSimpleCreator(MxParser.SimpleCreatorContext ctx) {
        return new newExpr(new position(ctx), (TypeNode) visit(ctx.simpleType()), 0, null);
    }

    @Override
    public ASTNode visitBlockStmt(MxParser.BlockStmtContext ctx) {
        return visit(ctx.block());
    }

    @Override
    public ASTNode visitVarDefStmt(MxParser.VarDefStmtContext ctx) {
        return visit(ctx.varDef());
    }

    @Override
    public ASTNode visitIfStmt(MxParser.IfStmtContext ctx) {
        return new ifStmt(new position(ctx), (ExprNode) visit(ctx.expression()), (StmtNode) visit(ctx.trueStmt), ctx.falseStmt != null ? (StmtNode) visit(ctx.falseStmt) : null);
    }

    @Override
    public ASTNode visitWhileStmt(MxParser.WhileStmtContext ctx) {
        return new whileStmt(new position(ctx), (ExprNode) visit(ctx.expression()), (StmtNode) visit(ctx.statement()));
    }

    @Override
    public ASTNode visitForStmt(MxParser.ForStmtContext ctx) {
        return new forStmt(new position(ctx), ctx.init != null ? (ExprNode) visit(ctx.init) : null, ctx.cond != null ? (ExprNode) visit(ctx.cond) : null, ctx.inc != null ? (ExprNode) visit(ctx.inc) : null, (StmtNode) visit(ctx.statement()));
    }

    @Override
    public ASTNode visitBreakStmt(MxParser.BreakStmtContext ctx) {
        return new breakStmt(new position(ctx));
    }

    @Override
    public ASTNode visitContinueStmt(MxParser.ContinueStmtContext ctx) {
        return new continueStmt(new position(ctx));
    }

    @Override
    public ASTNode visitReturnStmt(MxParser.ReturnStmtContext ctx) {
        return new returnStmt(new position(ctx), ctx.expression() != null ? (ExprNode) visit(ctx.expression()) : null);
    }

    @Override
    public ASTNode visitPureExprStmt(MxParser.PureExprStmtContext ctx) {
        return new pureExprStmt(new position(ctx), (ExprNode) visit(ctx.expression()));
    }

    @Override
    public ASTNode visitEmptyStmt(MxParser.EmptyStmtContext ctx) {
        return new emptyStmt(new position(ctx));
    }

    @Override
    public ASTNode visitNewExpr(MxParser.NewExprContext ctx) {
        return visit(ctx.creator());
    }

    @Override
    public ASTNode visitPrefixExpr(MxParser.PrefixExprContext ctx) {
        return new prefixExpr(new position(ctx), (ExprNode) visit(ctx.expression()), ctx.op.getText());
    }

    @Override
    public ASTNode visitSubscriptExpr(MxParser.SubscriptExprContext ctx) {
        return new subscriptExpr(new position(ctx), (ExprNode) visit(ctx.base), (ExprNode) visit(ctx.offset));
    }

    @Override
    public ASTNode visitMemberExpr(MxParser.MemberExprContext ctx) {
        return new memberExpr(new position(ctx), (ExprNode) visit(ctx.expression()), ctx.Identifier().getText());
    }

    @Override
    public ASTNode visitSuffixExpr(MxParser.SuffixExprContext ctx) {
        return new suffixExpr(new position(ctx), (ExprNode) visit(ctx.expression()), ctx.op.getText());
    }

    @Override
    public ASTNode visitAtomExpr(MxParser.AtomExprContext ctx) {
        return visit(ctx.primary());
    }

    @Override
    public ASTNode visitBinaryExpr(MxParser.BinaryExprContext ctx) {
        return new binaryExpr(new position(ctx), (ExprNode) visit(ctx.src1), (ExprNode) visit(ctx.src2), ctx.op.getText());
    }

    @Override
    public ASTNode visitFuncCallExpr(MxParser.FuncCallExprContext ctx) {
        ExprNode base = (ExprNode) visit(ctx.expression());
        if (base instanceof memberExpr) {
            ((memberExpr) base).isFunc = true;
            base.assignable = false;
        }
        return new funcCallExpr(new position(ctx), base, ctx.expressionList() != null ? (exprListExpr) visit(ctx.expressionList()) : new exprListExpr(new position(ctx)));
    }

    @Override
    public ASTNode visitExpressionList(MxParser.ExpressionListContext ctx) {
        exprListExpr ans = new exprListExpr(new position(ctx));
        for (ParserRuleContext x : ctx.expression()) {
            ans.exprList.add((ExprNode) visit(x));
        }
        return ans;
    }
}
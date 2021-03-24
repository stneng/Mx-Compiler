package AST;

import AST.symbol.Type;
import AST.symbol.funcSymbol;
import Util.position;

import java.util.ArrayList;

public class funcDef extends DefNode {
    public String name;
    public TypeNode type;
    public blockStmt block;
    public ArrayList<varDefSubStmt> paramList;
    public funcSymbol func = null;
    public Type returnType = null;

    public funcDef(position pos, String name, TypeNode type, blockStmt block, ArrayList<varDefSubStmt> paramList) {
        super(pos);
        this.name = name;
        this.type = type;
        this.block = block;
        this.paramList = paramList;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

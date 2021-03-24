package AST.symbol;

import IR.Function;

import java.util.ArrayList;

public class funcSymbol extends Type {
    public Type returnType;
    public String name;
    public ArrayList<varSymbol> paramList = new ArrayList<>();
    public Function func = null;

    public funcSymbol(String name) {
        this.name = name;
    }
}

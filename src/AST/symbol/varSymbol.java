package AST.symbol;

import IR.operand.Operand;

public class varSymbol {
    public String name;
    public Type type;
    public boolean isGlobal = false;
    public boolean isClassMember = false;
    public Operand operand = null;

    public varSymbol(String name) {
        this.name = name;
    }

    public varSymbol(String name, Type type) {
        this.name = name;
        this.type = type;
    }
}

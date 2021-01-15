package Util.symbol;

import java.util.HashMap;

public class classType extends Type {
    public String name;
    public HashMap<String, varSymbol> varMap = new HashMap<>();
    public HashMap<String, funcSymbol> funcMap = new HashMap<>();
    public funcSymbol constructor = null;

    public classType(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Type t) {
        return t.isNull() || ((t instanceof classType) && (this.name.equals(((classType) t).name)));
    }
}

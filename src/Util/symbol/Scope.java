package Util.symbol;

import AST.TypeNode;
import Util.error.semanticError;
import Util.position;

import java.util.HashMap;

public class Scope {
    public HashMap<String, varSymbol> varMap = new HashMap<>();
    public HashMap<String, funcSymbol> funcMap = new HashMap<>();
    public HashMap<String, Type> typeMap = new HashMap<>();
    public Scope parentScope;

    public Scope(Scope parentScope) {
        this.parentScope = parentScope;
    }

    public void defineVariable(String name, varSymbol value, position pos) {
        if (this.containsType(name, true, pos)) throw new semanticError("duplicated with type name", pos);
        if (varMap.containsKey(name)) throw new semanticError("variable redefine", pos);
        varMap.put(name, value);
    }

    public varSymbol getVariable(String name, boolean lookUpon, position pos) {
        if (varMap.containsKey(name)) return varMap.get(name);
        else if (parentScope != null && lookUpon) return parentScope.getVariable(name, true, pos);
        else throw new semanticError("variable not define", pos);
    }

    public void defineFunction(String name, funcSymbol value, position pos) {
        if (this.containsType(name, true, pos)) throw new semanticError("duplicated with type name", pos);
        if (funcMap.containsKey(name)) throw new semanticError("function redefine", pos);
        funcMap.put(name, value);
    }

    public funcSymbol getFunction(String name, boolean lookUpon, position pos) {
        if (funcMap.containsKey(name)) return funcMap.get(name);
        else if (parentScope != null && lookUpon) return parentScope.getFunction(name, true, pos);
        else throw new semanticError("function not define", pos);
    }

    public void defineType(String name, Type value, position pos) {
        if (typeMap.containsKey(name)) throw new semanticError("class redefine", pos);
        typeMap.put(name, value);
    }

    public boolean containsType(String name, boolean lookUpon, position pos) {
        if (typeMap.containsKey(name)) return true;
        else if (parentScope != null && lookUpon) return parentScope.containsType(name, true, pos);
        else return false;
    }

    public Type getType(String name, boolean lookUpon, position pos) {
        if (typeMap.containsKey(name)) return typeMap.get(name);
        else if (parentScope != null && lookUpon) return parentScope.getType(name, true, pos);
        else throw new semanticError("type not define", pos);
    }

    public Type getType(TypeNode type) {
        if (type.dim == 0) return typeMap.get(type.simpleType);
        else return new arrayType(typeMap.get(type.simpleType), type.dim);
    }
}

package IR;

import AST.symbol.Type;
import AST.symbol.arrayType;
import AST.symbol.classType;
import AST.symbol.primitiveType;
import IR.operand.ConstStr;
import IR.operand.Register;
import IR.type.*;

import java.util.HashMap;

public class IR {
    public HashMap<String, Function> func = new HashMap<>();
    public HashMap<String, Register> gVar = new HashMap<>();
    public HashMap<String, ClassType> mxClass = new HashMap<>();
    public HashMap<String, ConstStr> constStr = new HashMap<>();
    public Function mallocFunc;
    public Block __gVar_init_destBlock;

    public IR() {
        func.put("__gVar_init", new Function("__gVar_init"));
        __gVar_init_destBlock = func.get("__gVar_init").beginBlock;
        mallocFunc = new Function("__mx_builtin_malloc");
        mallocFunc.returnType = new Pointer(new IntType(8));
    }

    public BaseType getType(Type type) {
        if (type instanceof arrayType) {
            BaseType t = getType(((arrayType) type).simpleType);
            for (int i = 0; i < ((arrayType) type).dim; i++) t = new Pointer(t);
            return t;
        }
        if (type instanceof classType) {
            return new Pointer(((classType) type).classType);
        }
        if (type instanceof primitiveType) {
            if (type.isString()) return new StringType();
            if (type.isNull()) return new VoidType();
            if (type.isInt()) return new IntType(32);
            if (type.isBool()) return new BoolType();
            if (type.isVoid()) return new VoidType();
        }
        return new VoidType();
    }
}

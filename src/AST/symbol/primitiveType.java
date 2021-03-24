package AST.symbol;

public class primitiveType extends Type {
    public String name;

    public primitiveType(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Type t) {
        return (this.isNull() && (t instanceof arrayType || t instanceof classType)) || ((t instanceof primitiveType) && (this.name.equals(((primitiveType) t).name)));
    }

    @Override
    public boolean isBool() {
        return name.equals("bool");
    }

    @Override
    public boolean isInt() {
        return name.equals("int");
    }

    @Override
    public boolean isString() {
        return name.equals("string");
    }

    @Override
    public boolean isVoid() {
        return name.equals("void");
    }

    @Override
    public boolean isNull() {
        return name.equals("null");
    }
}

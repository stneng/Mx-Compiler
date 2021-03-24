package AST.symbol;

public class arrayType extends Type {
    public Type simpleType;
    public int dim;

    public arrayType(Type simpleType, int dim) {
        this.simpleType = simpleType;
        this.dim = dim;
    }

    @Override
    public boolean equals(Type t) {
        return t.isNull() || ((t instanceof arrayType) && (this.simpleType.equals(((arrayType) t).simpleType) && this.dim == ((arrayType) t).dim));
    }
}

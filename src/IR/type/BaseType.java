package IR.type;

import IR.operand.Operand;

public abstract class BaseType {
    public BaseType() {
    }

    public abstract int size();

    public abstract String toString();

    public abstract boolean equals(BaseType t);

    public abstract Operand getInit();
}

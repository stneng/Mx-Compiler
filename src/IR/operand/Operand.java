package IR.operand;

import IR.type.BaseType;

public abstract class Operand {
    public BaseType type;

    public Operand(BaseType type) {
        this.type = type;
    }

    public abstract String toString();

    public abstract boolean equals(Operand t);

    public boolean isConst() {
        return this instanceof ConstBool || this instanceof ConstInt || this instanceof ConstStr;
    }
}

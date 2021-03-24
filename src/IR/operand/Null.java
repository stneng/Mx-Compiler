package IR.operand;

import IR.type.BaseType;

public class Null extends Operand {
    public Null() {
        super(null);
    }

    public Null(BaseType type) {
        super(type);
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public boolean equals(Operand t) {
        return t instanceof Null;
    }
}

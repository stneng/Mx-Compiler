package IR.type;

import IR.operand.ConstInt;
import IR.operand.Operand;

public class IntType extends BaseType {
    public int size;

    public IntType(int size) {
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return "i" + size;
    }

    @Override
    public boolean equals(BaseType t) {
        return (t instanceof IntType && size == t.size());
    }

    @Override
    public Operand getInit() {
        return new ConstInt(0, 32);
    }
}

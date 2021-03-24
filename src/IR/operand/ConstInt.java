package IR.operand;

import IR.type.IntType;

public class ConstInt extends Operand {
    public int value;

    public ConstInt(int value, int size) {
        super(new IntType(size));
        this.value = value;
    }

    @Override
    public String toString() {
        return value + "";
    }

    @Override
    public boolean equals(Operand t) {
        return (t instanceof ConstInt && ((ConstInt) t).value == value);
    }
}

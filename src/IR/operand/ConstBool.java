package IR.operand;

import IR.type.BoolType;

public class ConstBool extends Operand {
    public boolean value;

    public ConstBool(boolean value) {
        super(new BoolType());
        this.value = value;
    }

    @Override
    public String toString() {
        return value ? "1" : "0";
    }

    @Override
    public boolean equals(Operand t) {
        return (t instanceof ConstBool && ((ConstBool) t).value == value);
    }
}

package IR.type;

import IR.operand.ConstBool;
import IR.operand.Operand;

public class BoolType extends BaseType {
    public BoolType() {
    }

    @Override
    public int size() {
        return 32;
    }

    @Override
    public String toString() {
        return "i1";
    }

    @Override
    public boolean equals(BaseType t) {
        return (t instanceof BoolType);
    }

    @Override
    public Operand getInit() {
        return new ConstBool(false);
    }
}

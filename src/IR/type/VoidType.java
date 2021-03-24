package IR.type;

import IR.operand.Operand;
import IR.operand.Void;
import Util.error.internalError;

public class VoidType extends BaseType {
    public VoidType() {
    }

    @Override
    public int size() {
        throw new internalError("void size");
    }

    @Override
    public String toString() {
        return "void";
    }

    @Override
    public boolean equals(BaseType t) {
        return (t instanceof VoidType);
    }

    @Override
    public Operand getInit() {
        return new Void();
    }
}

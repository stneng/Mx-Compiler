package IR.type;

import IR.operand.Null;
import IR.operand.Operand;

public class Pointer extends BaseType {
    public BaseType pointType;

    public Pointer(BaseType pointType) {
        this.pointType = pointType;
    }

    @Override
    public int size() {
        return 32;
    }

    @Override
    public String toString() {
        return pointType.toString() + "*";
    }

    @Override
    public boolean equals(BaseType t) {
        return (t instanceof Pointer && ((((Pointer) t).pointType instanceof VoidType) || (((Pointer) t).pointType.equals(pointType))));
    }

    @Override
    public Operand getInit() {
        return new Null();
    }
}

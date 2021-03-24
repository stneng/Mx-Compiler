package IR.type;

import IR.operand.Null;
import IR.operand.Operand;

public class StringType extends BaseType {

    public StringType() {

    }

    @Override
    public int size() {
        return new Pointer(null).size();
    }

    @Override
    public String toString() {
        return "i8*";
    }

    @Override
    public boolean equals(BaseType t) {
        return (t instanceof StringType);
    }

    @Override
    public Operand getInit() {
        return new Null();
    }
}

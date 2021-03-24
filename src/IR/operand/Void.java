package IR.operand;

public class Void extends Operand {
    public Void() {
        super(null);
    }

    @Override
    public String toString() {
        return "void";
    }

    @Override
    public boolean equals(Operand t) {
        return t instanceof Void;
    }
}

package ASM.operand;

public class Address extends Imm {
    public String name;

    public Address(int value, String name) {
        super(value);
        this.name = name;
    }

    @Override
    public String toString() {
        return "%" + (value > 0 ? "hi" : "lo") + "(" + name + ")";
    }
}

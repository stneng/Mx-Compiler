package ASM.operand;

public class Imm extends Operand {
    public int value;
    public boolean inParam = false;

    public Imm(int value) {
        this.value = value;
    }

    public Imm(int value, boolean inParam) {
        this.value = value;
        this.inParam = inParam;
    }

    @Override
    public String toString() {
        return value + "";
    }
}

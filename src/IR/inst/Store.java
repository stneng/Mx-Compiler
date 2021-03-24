package IR.inst;

import IR.Block;
import IR.operand.Operand;

import java.util.ArrayList;

public class Store extends Inst {
    public Operand address, value;

    public Store(Block block, Operand address, Operand value) {
        super(block, null);
        this.address = address;
        this.value = value;
    }

    @Override
    public String toString() {
        return "store " + value.type.toString() + " " + value.toString() + ", " + address.type.toString() + " " + address.toString();
    }

    @Override
    public ArrayList<Operand> getUseOperand() {
        ArrayList<Operand> ans = new ArrayList<>();
        ans.add(address);
        ans.add(value);
        return ans;
    }

    @Override
    public void replace(Operand a, Operand b) {
        if (address == a) address = b;
        if (value == a) value = b;
    }
}

package IR.type;

import IR.Function;
import IR.operand.Null;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;

public class ClassType extends BaseType {
    public String name;
    public int size = 0;
    public ArrayList<Register> var = new ArrayList<>();
    public ArrayList<Function> func = new ArrayList<>();
    public Function constructor = null;

    public ClassType(String name) {
        this.name = name;
    }

    public void addVar(Register a) {
        var.add(a);
        size += a.type.size();
    }

    public int getOffset(int id) {
        int ans = 0;
        for (int i = 0; i < id; i++) ans += var.get(i).type.size();
        return ans;
    }

    public int getVarId(String name) {
        name = this.name + "." + name;
        for (int i = 0; i < var.size(); i++) {
            if (var.get(i).name.equals(name)) return i;
        }
        return -1;
    }

    public Register getVarReg(String name) {
        name = this.name + "." + name;
        for (Register register : var) {
            if (register.name.equals(name)) return register;
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return "%struct." + name;
    }

    @Override
    public boolean equals(BaseType t) {
        return (t instanceof ClassType && ((ClassType) t).name.equals(name));
    }

    @Override
    public Operand getInit() {
        return new Null();
    }
}

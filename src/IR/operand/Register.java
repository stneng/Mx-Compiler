package IR.operand;

import IR.inst.Inst;
import IR.type.BaseType;

import java.util.ArrayList;
import java.util.Stack;

public class Register extends Operand {
    public String name;
    public boolean isGlobal = false;
    public boolean isConstPtr = false;
    public ArrayList<Inst> assign = new ArrayList<>();
    public Stack<Register> rename_stack = new Stack<>();
    public int rename_id = 0;
    public boolean isUsed = false;

    public Register(BaseType type, String name) {
        super(type);
        this.name = name;
    }

    @Override
    public String toString() {
        if (isGlobal) return "@" + name;
        else return "%" + name;
    }

    @Override
    public boolean equals(Operand t) {
        return this == t;
    }
}

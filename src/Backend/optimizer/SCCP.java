package Backend.optimizer;

import IR.Block;
import IR.Function;
import IR.IR;
import IR.inst.*;
import IR.operand.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SCCP {
    public IR ir;
    public Function currentFunction = null;

    public SCCP(IR ir) {
        this.ir = ir;
    }

    public HashMap<Register, ArrayList<Inst>> regUse;

    public void regUseCollect() {
        regUse = new HashMap<>();
        for (Block block : currentFunction.blocks) {
            for (Inst inst : block.inst) {
                for (Operand reg : inst.getUseOperand())
                    if (reg instanceof Register) {
                        if (!regUse.containsKey(reg)) regUse.put((Register) reg, new ArrayList<>());
                        regUse.get(reg).add(inst);
                    }
            }
        }
    }

    public void replace(Register reg, Operand value) {
        for (Inst inst : regUse.get(reg)) {
            inst.replace(reg, value);
        }
    }

    public boolean doBinary(Binary inst) {
        if (inst.src1 instanceof ConstInt && inst.src2 instanceof ConstInt) {
            if ((inst.op.equals("sdiv") || inst.op.equals("srem")) && ((ConstInt) inst.src2).value == 0) return false;
            int ans = switch (inst.op) {
                case "mul" -> ((ConstInt) inst.src1).value * ((ConstInt) inst.src2).value;
                case "sdiv" -> ((ConstInt) inst.src1).value / ((ConstInt) inst.src2).value;
                case "srem" -> ((ConstInt) inst.src1).value % ((ConstInt) inst.src2).value;
                case "sub" -> ((ConstInt) inst.src1).value - ((ConstInt) inst.src2).value;
                case "shl" -> ((ConstInt) inst.src1).value << ((ConstInt) inst.src2).value;
                case "ashr" -> ((ConstInt) inst.src1).value >> ((ConstInt) inst.src2).value;
                case "and" -> ((ConstInt) inst.src1).value & ((ConstInt) inst.src2).value;
                case "xor" -> ((ConstInt) inst.src1).value ^ ((ConstInt) inst.src2).value;
                case "or" -> ((ConstInt) inst.src1).value | ((ConstInt) inst.src2).value;
                case "add" -> ((ConstInt) inst.src1).value + ((ConstInt) inst.src2).value;
                default -> 0;
            };
            replace(inst.reg, new ConstInt(ans, 32));
            return true;
        }
        return false;
    }

    public boolean doCmp(Cmp inst) {
        if (inst.src1 instanceof ConstInt && inst.src2 instanceof ConstInt) {
            boolean ans = switch (inst.op) {
                case "slt" -> ((ConstInt) inst.src1).value < ((ConstInt) inst.src2).value;
                case "sgt" -> ((ConstInt) inst.src1).value > ((ConstInt) inst.src2).value;
                case "sle" -> ((ConstInt) inst.src1).value <= ((ConstInt) inst.src2).value;
                case "sge" -> ((ConstInt) inst.src1).value >= ((ConstInt) inst.src2).value;
                case "eq" -> ((ConstInt) inst.src1).value == ((ConstInt) inst.src2).value;
                case "ne" -> ((ConstInt) inst.src1).value != ((ConstInt) inst.src2).value;
                default -> false;
            };
            replace(inst.reg, new ConstBool(ans));
            return true;
        }
        if (inst.src1 instanceof ConstBool && inst.src2 instanceof ConstBool) {
            boolean ans = switch (inst.op) {
                case "eq" -> ((ConstBool) inst.src1).value == ((ConstBool) inst.src2).value;
                case "ne" -> ((ConstBool) inst.src1).value != ((ConstBool) inst.src2).value;
                default -> false;
            };
            replace(inst.reg, new ConstBool(ans));
            return true;
        }
        return false;
    }

    public boolean doConstStrCall(Call inst) {
        switch (inst.func.name) {
            case "__mx_builtin_str_add":
                if (inst.param.get(0) instanceof ConstStr && inst.param.get(1) instanceof ConstStr) {
                    ConstStr ans = new ConstStr(((ConstStr) inst.param.get(0)).name + "+" + ((ConstStr) inst.param.get(1)).name, ((ConstStr) inst.param.get(0)).value + ((ConstStr) inst.param.get(1)).value);
                    replace(inst.reg, ans);
                    return true;
                }
            case "__mx_builtin_str_lt":
                if (inst.param.get(0) instanceof ConstStr && inst.param.get(1) instanceof ConstStr) {
                    boolean ans = ((ConstStr) inst.param.get(0)).value.compareTo(((ConstStr) inst.param.get(1)).value) < 0;
                    replace(inst.reg, new ConstBool(ans));
                    return true;
                }
            case "__mx_builtin_str_gt":
                if (inst.param.get(0) instanceof ConstStr && inst.param.get(1) instanceof ConstStr) {
                    boolean ans = ((ConstStr) inst.param.get(0)).value.compareTo(((ConstStr) inst.param.get(1)).value) > 0;
                    replace(inst.reg, new ConstBool(ans));
                    return true;
                }
            case "__mx_builtin_str_le":
                if (inst.param.get(0) instanceof ConstStr && inst.param.get(1) instanceof ConstStr) {
                    boolean ans = ((ConstStr) inst.param.get(0)).value.compareTo(((ConstStr) inst.param.get(1)).value) <= 0;
                    replace(inst.reg, new ConstBool(ans));
                    return true;
                }
            case "__mx_builtin_str_ge":
                if (inst.param.get(0) instanceof ConstStr && inst.param.get(1) instanceof ConstStr) {
                    boolean ans = ((ConstStr) inst.param.get(0)).value.compareTo(((ConstStr) inst.param.get(1)).value) >= 0;
                    replace(inst.reg, new ConstBool(ans));
                    return true;
                }
            case "__mx_builtin_str_eq":
                if (inst.param.get(0) instanceof ConstStr && inst.param.get(1) instanceof ConstStr) {
                    boolean ans = ((ConstStr) inst.param.get(0)).value.compareTo(((ConstStr) inst.param.get(1)).value) == 0;
                    replace(inst.reg, new ConstBool(ans));
                    return true;
                }
            case "__mx_builtin_str_ne":
                if (inst.param.get(0) instanceof ConstStr && inst.param.get(1) instanceof ConstStr) {
                    boolean ans = ((ConstStr) inst.param.get(0)).value.compareTo(((ConstStr) inst.param.get(1)).value) != 0;
                    replace(inst.reg, new ConstBool(ans));
                    return true;
                }
            default:
                return false;
        }
    }

    public boolean doPhi(Phi inst) {
        Operand t = inst.values.get(0);
        if (!t.isConst()) return false;
        for (int i = 1; i < inst.values.size(); i++) {
            if (!t.equals(inst.values.get(i))) return false;
        }
        replace(inst.reg, t);
        return true;
    }

    public HashSet<Block> visited;
    public boolean cond;
    public boolean done;

    public void doBlock(Block block) {
        visited.add(block);
        for (int i = 0; i < block.inst.size(); i++) {
            Inst inst = block.inst.get(i);
            if (inst instanceof Assign) {
                if (((Assign) inst).value.isConst()) {
                    replace(inst.reg, ((Assign) inst).value);
                    block.inst.remove(i);
                    i--;
                    cond = true;
                }
            } else if (inst instanceof Binary) {
                if (doBinary((Binary) inst)) {
                    block.inst.remove(i);
                    i--;
                    cond = true;
                }
            } else if (inst instanceof Branch) {
                if (((Branch) inst).condition instanceof ConstBool) {
                    block.removeTerminator();
                    if (((ConstBool) ((Branch) inst).condition).value) {
                        block.addTerminator(new Jump(block, ((Branch) inst).trueDest));
                    } else {
                        block.addTerminator(new Jump(block, ((Branch) inst).falseDest));
                    }
                    cond = true;
                }
            } else if (inst instanceof Call) {
                if (doConstStrCall((Call) inst)) {
                    block.inst.remove(i);
                    i--;
                    cond = true;
                }
            } else if (inst instanceof Cmp) {
                if (doCmp((Cmp) inst)) {
                    block.inst.remove(i);
                    i--;
                    cond = true;
                }
            } else if (inst instanceof Phi) {
                if (doPhi((Phi) inst)) {
                    block.inst.remove(i);
                    i--;
                    cond = true;
                }
            }
        }
        block.nxt.forEach(x -> {
            if (!visited.contains(x)) doBlock(x);
        });
    }

    public void doFunc(Function func) {
        currentFunction = func;
        cond = true;
        while (cond) {
            cond = false;
            regUseCollect();
            visited = new HashSet<>();
            doBlock(func.beginBlock);
            done |= cond;
        }
        currentFunction = null;
    }

    public boolean run() {
        done = false;
        ir.func.forEach((s, x) -> doFunc(x));
        return done;
    }
}

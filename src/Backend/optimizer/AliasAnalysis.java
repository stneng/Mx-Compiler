package Backend.optimizer;

import IR.Block;
import IR.Function;
import IR.IR;
import IR.inst.Call;
import IR.inst.Inst;
import IR.inst.Load;
import IR.inst.Store;
import IR.operand.Operand;
import IR.operand.Register;
import IR.type.ClassType;
import IR.type.Pointer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AliasAnalysis {
    public IR ir;
    public Function currentFunction = null;

    public AliasAnalysis(IR ir) {
        this.ir = ir;
    }

    public HashMap<Function, ArrayList<Function>> edge = new HashMap<>();
    public HashMap<Operand, Operand> faPtr = new HashMap<>(), faData = new HashMap<>();
    public HashMap<Function, HashSet<Operand>> funcPtr = new HashMap<>(), funcData = new HashMap<>(), funcPtrS = new HashMap<>(), funcDataS = new HashMap<>();

    public Operand getFatherPtr(Operand x) {
        if (!faPtr.containsKey(x)) faPtr.put(x, x);
        if (faPtr.get(x) == x) return x;
        Operand t = getFatherPtr(faPtr.get(x));
        faPtr.replace(x, t);
        return t;
    }

    public void tryMergePtr(Operand a, Operand b) {
        if (!(a.type instanceof Pointer && b.type instanceof Pointer && (((Pointer) a.type).pointType instanceof Pointer || ((Pointer) a.type).pointType instanceof ClassType) && (((Pointer) b.type).pointType instanceof Pointer || ((Pointer) b.type).pointType instanceof ClassType)))
            return;
        Operand f1 = getFatherPtr(a), f2 = getFatherPtr(b);
        faPtr.replace(f1, f2);
    }

    public Operand getFatherData(Operand x) {
        if (!faData.containsKey(x)) faData.put(x, x);
        if (faData.get(x) == x) return x;
        Operand t = getFatherData(faData.get(x));
        faData.replace(x, t);
        return t;
    }

    public void tryMergeData(Operand a, Operand b) {
        if (!(a.type instanceof Pointer && b.type instanceof Pointer)) return;
        Operand f1 = getFatherData(a), f2 = getFatherData(b);
        faData.replace(f1, f2);
    }

    public void doBlock(Block block) {
        for (Inst inst : block.inst) {
            if (inst instanceof Call) {
                if (!((Call) inst).func.name.startsWith("__mx_builtin_")) {
                    edge.get(currentFunction).add(((Call) inst).func);
                    for (int i = 0; i < ((Call) inst).param.size(); i++) {
                        tryMergePtr(((Call) inst).param.get(i), ((Call) inst).func.params.get(i));
                        tryMergeData(((Call) inst).param.get(i), ((Call) inst).func.params.get(i));
                    }
                }
            }
            if (inst.reg != null && inst.reg.type instanceof Pointer) {
                if (inst instanceof Call) {
                    if (!((Call) inst).func.name.startsWith("__mx_builtin_")) {
                        tryMergePtr(inst.reg, ((Call) inst).func.returnInst.get(0).value);
                    }
                } else {
                    for (Operand op : inst.getUseOperand()) {
                        tryMergePtr(inst.reg, op);
                    }
                }
            }
            if (inst.reg != null && inst.reg.type instanceof Pointer) {
                if (inst instanceof Call) {
                    if (!((Call) inst).func.name.startsWith("__mx_builtin_")) {
                        tryMergeData(inst.reg, ((Call) inst).func.returnInst.get(0).value);
                    }
                } else {
                    for (Operand op : inst.getUseOperand()) {
                        tryMergeData(inst.reg, op);
                    }
                }
            }
            if (inst instanceof Load) {
                if (((Load) inst).address.type instanceof Pointer && (((Pointer) ((Load) inst).address.type).pointType instanceof Pointer || ((Pointer) ((Load) inst).address.type).pointType instanceof ClassType)) {
                    funcPtr.get(currentFunction).add(((Load) inst).address);
                } else {
                    funcData.get(currentFunction).add(((Load) inst).address);
                }
            } else if (inst instanceof Store) {
                if (((Store) inst).address.type instanceof Pointer && (((Pointer) ((Store) inst).address.type).pointType instanceof Pointer || ((Pointer) ((Store) inst).address.type).pointType instanceof ClassType)) {
                    funcPtr.get(currentFunction).add(((Store) inst).address);
                    funcPtrS.get(currentFunction).add(((Store) inst).address);
                } else {
                    funcData.get(currentFunction).add(((Store) inst).address);
                    funcDataS.get(currentFunction).add(((Store) inst).address);
                }
            }
        }
    }

    public boolean mayConflictPtr(Operand a, Operand b) {
        if (a instanceof Register && ((Register) a).isGlobal) return a.equals(b);
        if (b instanceof Register && ((Register) b).isGlobal) return b.equals(a);
        Operand f1 = getFatherPtr(a), f2 = getFatherPtr(b);
        return f1 == f2;
    }

    public boolean mayConflictData(Operand a, Operand b) {
        if (a instanceof Register && ((Register) a).isGlobal) return a.equals(b);
        if (b instanceof Register && ((Register) b).isGlobal) return b.equals(a);
        Operand f1 = getFatherData(a), f2 = getFatherData(b);
        return f1 == f2;
    }

    public boolean funcConflict(Function func, Operand x) {
        if (func.name.startsWith("__mx_builtin_")) return false;
        if (x.type instanceof Pointer && (((Pointer) x.type).pointType instanceof Pointer || ((Pointer) x.type).pointType instanceof ClassType)) {
            Operand t = getFatherPtr(x);
            return funcPtr.get(func).contains(t);
        } else {
            Operand t = getFatherData(x);
            return funcPtr.get(func).contains(t) || funcData.get(func).contains(t);
        }
    }

    public boolean funcConflictS(Function func, Operand x) {
        if (func.name.startsWith("__mx_builtin_")) return false;
        if (x.type instanceof Pointer && (((Pointer) x.type).pointType instanceof Pointer || ((Pointer) x.type).pointType instanceof ClassType)) {
            Operand t = getFatherPtr(x);
            return funcPtrS.get(func).contains(t);
        } else {
            Operand t = getFatherData(x);
            return funcPtrS.get(func).contains(t) || funcDataS.get(func).contains(t);
        }
    }

    public boolean funcHavePtr(Function func, Operand x) {
        if (func.name.startsWith("__mx_builtin_")) return false;
        Operand t = getFatherPtr(x);
        return funcPtr.get(func).contains(t);
    }

    public boolean funcHaveData(Function func, Operand x) {
        if (func.name.startsWith("__mx_builtin_")) return false;
        Operand t = getFatherData(x);
        return funcPtr.get(func).contains(t) || funcData.get(func).contains(t);
    }

    public void run() {
        ir.func.forEach((s, x) -> {
            edge.put(x, new ArrayList<>());
            funcPtr.put(x, new HashSet<>());
            funcData.put(x, new HashSet<>());
            funcPtrS.put(x, new HashSet<>());
            funcDataS.put(x, new HashSet<>());
        });
        ir.func.forEach((s, x) -> {
            currentFunction = x;
            x.blocks.forEach(this::doBlock);
            currentFunction = null;
        });
        funcPtr.forEach((func, set) -> {
            HashSet<Operand> t = new HashSet<>();
            set.forEach(x -> t.add(getFatherPtr(x)));
            set.clear();
            set.addAll(t);
        });
        funcData.forEach((func, set) -> {
            HashSet<Operand> t = new HashSet<>();
            set.forEach(x -> t.add(getFatherData(x)));
            set.clear();
            set.addAll(t);
        });
        funcPtrS.forEach((func, set) -> {
            HashSet<Operand> t = new HashSet<>();
            set.forEach(x -> t.add(getFatherPtr(x)));
            set.clear();
            set.addAll(t);
        });
        funcDataS.forEach((func, set) -> {
            HashSet<Operand> t = new HashSet<>();
            set.forEach(x -> t.add(getFatherData(x)));
            set.clear();
            set.addAll(t);
        });
        for (int i = 0; i < ir.func.size(); i++) {
            funcPtr.forEach((func, set) -> {
                ArrayList<Function> a = edge.get(func);
                a.forEach(b -> set.addAll(funcPtr.get(b)));
            });
            funcData.forEach((func, set) -> {
                ArrayList<Function> a = edge.get(func);
                a.forEach(b -> set.addAll(funcData.get(b)));
            });
            funcPtrS.forEach((func, set) -> {
                ArrayList<Function> a = edge.get(func);
                a.forEach(b -> set.addAll(funcPtrS.get(b)));
            });
            funcDataS.forEach((func, set) -> {
                ArrayList<Function> a = edge.get(func);
                a.forEach(b -> set.addAll(funcDataS.get(b)));
            });
        }
    }
}

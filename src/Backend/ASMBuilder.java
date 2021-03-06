package Backend;

import ASM.ASM;
import ASM.Block;
import ASM.Function;
import ASM.inst.Branch;
import ASM.inst.Call;
import ASM.inst.Inst;
import ASM.inst.Load;
import ASM.inst.Store;
import ASM.inst.*;
import ASM.operand.*;
import IR.inst.*;
import IR.operand.ConstBool;
import IR.operand.ConstInt;
import IR.operand.Null;
import IR.type.BaseType;
import IR.type.ClassType;
import IR.type.Pointer;
import Util.error.internalError;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class ASMBuilder {
    public IR.IR ir;
    public ASM asm;

    public Function currentFunction = null;
    public Block currentBlock = null;
    public HashMap<IR.operand.Operand, Register> regMap = new HashMap<>();
    public HashMap<IR.Block, Block> blockMap = new HashMap<>();
    public HashMap<IR.Function, Function> funcMap = new HashMap<>();

    public ASMBuilder(IR.IR ir, ASM asm) {
        this.ir = ir;
        this.asm = asm;
    }

    public Function getFunction(IR.Function func) {
        if (funcMap.get(func) == null) funcMap.put(func, new Function(func.name));
        return funcMap.get(func);
    }

    public Block getBlock(IR.Block block) {
        if (blockMap.get(block) == null) blockMap.put(block, new Block(block.loopDepth));
        return blockMap.get(block);
    }

    public Register getReg(IR.operand.Operand op) {
        if (op instanceof IR.operand.Register) {
            if (((IR.operand.Register) op).isGlobal) {
                throw new internalError("getReg");
            } else {
                if (regMap.get(op) == null) regMap.put(op, new VReg(((IR.operand.Register) op).name));
                return regMap.get(op);
            }
        } else {
            int value = 0;
            if (op instanceof IR.operand.ConstInt) value = ((ConstInt) op).value;
            if (op instanceof IR.operand.ConstBool) value = ((ConstBool) op).value ? 1 : 0;
            if (op instanceof IR.operand.ConstStr) {
                VReg tmp = new VReg("tmp"), tmp2 = new VReg("tmp");
                currentBlock.addInst(new Lui(tmp, new Address(1, ((IR.operand.ConstStr) op).name)));
                currentBlock.addInst(new Calc(tmp2, "addi", tmp, new Address(0, ((IR.operand.ConstStr) op).name)));
                return tmp2;
            }
            VReg tmp = new VReg("tmp");
            currentBlock.addInst(new Li(tmp, new Imm(value)));
            return tmp;
        }
    }

    public void assign(Register reg, IR.operand.Operand op) {
        if (op instanceof IR.operand.Register) {
            currentBlock.addInst(new Mv(reg, getReg(op)));
        } else {
            int value = 0;
            if (op instanceof IR.operand.ConstInt) value = ((ConstInt) op).value;
            if (op instanceof IR.operand.ConstBool) value = ((ConstBool) op).value ? 1 : 0;
            if (op instanceof IR.operand.ConstStr) {
                VReg tmp = new VReg("tmp");
                currentBlock.addInst(new Lui(tmp, new Address(1, ((IR.operand.ConstStr) op).name)));
                currentBlock.addInst(new Calc(reg, "addi", tmp, new Address(0, ((IR.operand.ConstStr) op).name)));
                return;
            }
            currentBlock.addInst(new Li(reg, new Imm(value)));
        }
    }

    public void do_inst(IR.inst.Inst inst) {
        if (inst instanceof IR.inst.Assign) {
            assign(getReg(inst.reg), ((Assign) inst).value);
        } else if (inst instanceof IR.inst.Binary) {
            Register rd = getReg(inst.reg), rs1;
            Operand rs2;
            String op = switch (((Binary) inst).op) {
                case "sdiv" -> "div";
                case "srem" -> "rem";
                case "shl" -> "sll";
                case "ashr" -> "sra";
                default -> ((Binary) inst).op;
            };
            if (op.equals("mul") || op.equals("div") || op.equals("rem")) {
                rs1 = getReg(((Binary) inst).src1);
                rs2 = getReg(((Binary) inst).src2);
            } else {
                if (((Binary) inst).src2 instanceof IR.operand.ConstInt) {
                    if (((ConstInt) ((Binary) inst).src2).value >= -2047 && ((ConstInt) ((Binary) inst).src2).value <= 2047) {
                        rs1 = getReg(((Binary) inst).src1);
                        rs2 = new Imm(((ConstInt) ((Binary) inst).src2).value);
                        if (op.equals("sub")) {
                            op = "addi";
                            ((Imm) rs2).value = -((Imm) rs2).value;
                        } else {
                            op = op + "i";
                        }
                    } else {
                        rs1 = getReg(((Binary) inst).src1);
                        rs2 = new VReg("tmp");
                        currentBlock.addInst(new Li((Register) rs2, new Imm(((ConstInt) ((Binary) inst).src2).value)));
                    }
                } else if (((Binary) inst).src1 instanceof IR.operand.ConstInt) {
                    if (/*!op.equals("div") && !op.equals("rem") && */!op.equals("sll") && !op.equals("sra") && !op.equals("sub")) {
                        if (((ConstInt) ((Binary) inst).src1).value >= -2048 && ((ConstInt) ((Binary) inst).src1).value <= 2047) {
                            rs1 = getReg(((Binary) inst).src2);
                            rs2 = new Imm(((ConstInt) ((Binary) inst).src1).value);
                            op = op + "i";
                        } else {
                            rs1 = getReg(((Binary) inst).src2);
                            rs2 = new VReg("tmp");
                            currentBlock.addInst(new Li((Register) rs2, new Imm(((ConstInt) ((Binary) inst).src1).value)));
                        }
                    } else {
                        rs1 = getReg(((Binary) inst).src1);
                        rs2 = getReg(((Binary) inst).src2);
                    }
                } else {
                    rs1 = getReg(((Binary) inst).src1);
                    rs2 = getReg(((Binary) inst).src2);
                }
            }
            currentBlock.addInst(new Calc(rd, op, rs1, rs2));
        } else if (inst instanceof IR.inst.BitCast) {
            currentBlock.addInst(new Mv(getReg(inst.reg), getReg(((BitCast) inst).value)));
        } else if (inst instanceof IR.inst.Branch) {
            IR.inst.Cmp cmp = null;
            for (int i = 0; i < inst.block.inst.size() - 1; i++) {
                IR.inst.Inst inst2 = inst.block.inst.get(i);
                if (inst2 instanceof IR.inst.Cmp && inst2.reg == ((IR.inst.Branch) inst).condition) {
                    cmp = (IR.inst.Cmp) inst2;
                }
            }
            if (cmp != null) {
                String op = switch (cmp.op) {
                    case "slt" -> "blt";
                    case "sgt" -> "bgt";
                    case "sle" -> "ble";
                    case "sge" -> "bge";
                    case "eq" -> "beq";
                    case "ne" -> "bne";
                    default -> "error";
                };
                currentBlock.addInst(new Branch(op, getReg(cmp.src1), getReg(cmp.src2), getBlock(((IR.inst.Branch) inst).trueDest)));
                currentBlock.addInst(new J(getBlock(((IR.inst.Branch) inst).falseDest)));
            } else {
                currentBlock.addInst(new Branch("bne", getReg(((IR.inst.Branch) inst).condition), asm.getPReg("zero"), getBlock(((IR.inst.Branch) inst).trueDest)));
                currentBlock.addInst(new J(getBlock(((IR.inst.Branch) inst).falseDest)));
            }
        } else if (inst instanceof IR.inst.Call) {
            for (int i = 0; i < Integer.min(((IR.inst.Call) inst).param.size(), 8); i++) {
                currentBlock.addInst(new Mv(asm.getPReg("a" + i), getReg(((IR.inst.Call) inst).param.get(i))));
            }
            if (((IR.inst.Call) inst).param.size() > 8) {
                int offset = 0, base = -(((IR.inst.Call) inst).param.size() - 8) * 4;
                for (int i = 8; i < ((IR.inst.Call) inst).param.size(); i++) {
                    currentBlock.addInst(new Store(getReg(((IR.inst.Call) inst).param.get(i)), asm.getPReg("sp"), new Imm(offset + base), 4));
                    offset += 4;
                }
            }
            currentBlock.addInst(new Call(getFunction(((IR.inst.Call) inst).func), asm));
            if (inst.reg != null) {
                currentBlock.addInst(new Mv(getReg(inst.reg), asm.getPReg("a0")));
            }
        } else if (inst instanceof IR.inst.Cmp) {
            switch (((IR.inst.Cmp) inst).op) {
                case "slt":
                    currentBlock.addInst(new Calc(getReg(inst.reg), "slt", getReg(((Cmp) inst).src1), getReg(((Cmp) inst).src2)));
                    break;
                case "sgt":
                    currentBlock.addInst(new Calc(getReg(inst.reg), "slt", getReg(((Cmp) inst).src2), getReg(((Cmp) inst).src1)));
                    break;
                case "sle":
                    VReg tmp = new VReg("tmp");
                    currentBlock.addInst(new Calc(tmp, "slt", getReg(((Cmp) inst).src2), getReg(((Cmp) inst).src1)));
                    currentBlock.addInst(new Calc(getReg(inst.reg), "xori", tmp, new Imm(1)));
                    break;
                case "sge":
                    tmp = new VReg("tmp");
                    currentBlock.addInst(new Calc(tmp, "slt", getReg(((Cmp) inst).src1), getReg(((Cmp) inst).src2)));
                    currentBlock.addInst(new Calc(getReg(inst.reg), "xori", tmp, new Imm(1)));
                    break;
                case "eq":
                    tmp = new VReg("tmp");
                    currentBlock.addInst(new Calc(tmp, "xor", getReg(((Cmp) inst).src1), getReg(((Cmp) inst).src2)));
                    currentBlock.addInst(new Calc(getReg(inst.reg), "sltiu", tmp, new Imm(1)));
                    break;
                case "ne":
                    tmp = new VReg("tmp");
                    currentBlock.addInst(new Calc(tmp, "xor", getReg(((Cmp) inst).src1), getReg(((Cmp) inst).src2)));
                    currentBlock.addInst(new Calc(getReg(inst.reg), "sltu", asm.getPReg("zero"), tmp));
                    break;
                default:
                    break;
            }
        } else if (inst instanceof IR.inst.GetElementPtr) {
            if (((GetElementPtr) inst).base instanceof Null) return;
            Register base = getReg(((GetElementPtr) inst).base);
            BaseType ty = ((Pointer) ((GetElementPtr) inst).base.type).pointType;
            int offset = 0;
            if (((GetElementPtr) inst).offset != null)
                offset = ((ClassType) ty).getOffset(((GetElementPtr) inst).offset.value) / 8;
            if (((GetElementPtr) inst).index instanceof ConstInt) {
                int w = ty.size() / 8 * ((ConstInt) ((GetElementPtr) inst).index).value + offset;
                currentBlock.addInst(new Calc(getReg(inst.reg), "addi", base, new Imm(w)));
            } else {
                VReg tmp = new VReg("tmp");
                int sz = ty.size() / 8;
                int log2 = (int) (Math.log(sz) / Math.log(2));
                if ((1 << log2) == sz)
                    currentBlock.addInst(new Calc(tmp, "slli", getReg(((GetElementPtr) inst).index), new Imm(log2)));
                else
                    currentBlock.addInst(new Calc(tmp, "mul", getReg(((GetElementPtr) inst).index), getReg(new ConstInt(sz, 32))));
                if (offset == 0) {
                    currentBlock.addInst(new Calc(getReg(inst.reg), "add", base, tmp));
                } else {
                    VReg tmp2 = new VReg("tmp");
                    currentBlock.addInst(new Calc(tmp2, "add", base, tmp));
                    currentBlock.addInst(new Calc(getReg(inst.reg), "addi", tmp2, new Imm(offset)));
                }
            }
        } else if (inst instanceof IR.inst.Jump) {
            currentBlock.addInst(new J(getBlock(((Jump) inst).dest)));
        } else if (inst instanceof IR.inst.Load) {
            if (((IR.inst.Load) inst).address instanceof IR.operand.Register && ((IR.operand.Register) ((IR.inst.Load) inst).address).isGlobal) {
                VReg tmp = new VReg("tmp");
                currentBlock.addInst(new Lui(tmp, new Address(1, ((IR.operand.Register) ((IR.inst.Load) inst).address).name)));
                currentBlock.addInst(new Load(getReg(inst.reg), tmp, new Address(0, ((IR.operand.Register) ((IR.inst.Load) inst).address).name), 4));
            } else {
                currentBlock.addInst(new Load(getReg(inst.reg), getReg(((IR.inst.Load) inst).address), new Imm(0), 4));
            }
        } else if (inst instanceof IR.inst.Return) {
            if (((Return) inst).value != null) assign(asm.getPReg("a0"), ((Return) inst).value);
            for (int i = 0; i < asm.getCalleeSave().size(); i++)
                currentBlock.addInst(new Mv(asm.getCalleeSave().get(i), currentFunction.calleeSaveVReg.get(i)));
            currentBlock.addInst(new Mv(asm.getPReg("ra"), currentFunction.raSaveVReg));
            currentBlock.addInst(new Ret(asm));
            currentFunction.endBlock = currentBlock;
        } else if (inst instanceof IR.inst.Store) {
            if (((IR.inst.Store) inst).address instanceof IR.operand.Register && ((IR.operand.Register) ((IR.inst.Store) inst).address).isGlobal) {
                VReg tmp = new VReg("tmp");
                currentBlock.addInst(new Lui(tmp, new Address(1, ((IR.operand.Register) ((IR.inst.Store) inst).address).name)));
                currentBlock.addInst(new Store(getReg(((IR.inst.Store) inst).value), tmp, new Address(0, ((IR.operand.Register) ((IR.inst.Store) inst).address).name), 4));
            } else {
                currentBlock.addInst(new Store(getReg(((IR.inst.Store) inst).value), getReg(((IR.inst.Store) inst).address), new Imm(0), 4));
            }
        } else {
            throw new internalError("inst selector error");
        }
    }

    public void do_block(IR.Block block) {
        currentBlock = getBlock(block);
        currentBlock.name = "." + currentFunction.name + "." + block.name;
        block.pre.forEach(x -> currentBlock.pre.add(getBlock(x)));
        block.nxt.forEach(x -> currentBlock.nxt.add(getBlock(x)));
        currentFunction.blocks.add(currentBlock);
        block.inst.forEach(this::do_inst);
        currentBlock = null;
    }

    public void do_func(IR.Function func) {
        regMap = new HashMap<>();
        blockMap = new HashMap<>();
        currentFunction = getFunction(func);
        currentFunction.beginBlock = getBlock(func.beginBlock);
        currentBlock = currentFunction.beginBlock;
        asm.func.put(currentFunction.name, currentFunction);
        func.params.forEach(x -> currentFunction.params.add(getReg(x)));
        for (int i = 0; i < asm.getCalleeSave().size(); i++) {
            VReg tmp = new VReg("tmp");
            currentFunction.calleeSaveVReg.add(tmp);
            currentBlock.addInst(new Mv(tmp, asm.getCalleeSave().get(i)));
        }
        VReg tmp = new VReg("tmp");
        currentFunction.raSaveVReg = tmp;
        currentBlock.addInst(new Mv(tmp, asm.getPReg("ra")));
        for (int i = 0; i < Integer.min(currentFunction.params.size(), 8); i++) {
            currentBlock.addInst(new Mv(currentFunction.params.get(i), asm.getPReg("a" + i)));
        }
        if (currentFunction.params.size() > 8) {
            int offset = 0;
            for (int i = 8; i < currentFunction.params.size(); i++) {
                currentBlock.addInst(new Load(currentFunction.params.get(i), asm.getPReg("sp"), new Imm(offset, true), 4));
                offset += 4;
            }
        }
        func.blocks.forEach(this::do_block);
        removeUselessInst();
        currentFunction = null;
    }

    public void removeUselessInst() {
        AtomicBoolean cond = new AtomicBoolean(true);
        while (cond.get()) {
            cond.set(false);
            HashSet<Register> used = new HashSet<>();
            used.add(asm.getPReg("a0"));
            currentFunction.blocks.forEach(t -> t.inst.forEach(x -> used.addAll(x.getUse())));
            currentFunction.blocks.forEach(t -> {
                for (int i = 0; i < t.inst.size(); i++) {
                    Inst x = t.inst.get(i);
                    if ((x instanceof Calc && !used.contains(((Calc) x).rd)) ||
                            (x instanceof Li && !used.contains(((Li) x).reg)) ||
                            (x instanceof Lui && !used.contains(((Lui) x).reg)) ||
                            (x instanceof Load && !used.contains(((Load) x).reg))) {
                        t.removeInst(x);
                        i--;
                        cond.set(true);
                    }
                }
            });
        }
    }

    public void run() {
        asm.gVar = ir.gVar;
        asm.constStr = ir.constStr;
        ir.func.forEach((s, x) -> do_func(x));
    }
}

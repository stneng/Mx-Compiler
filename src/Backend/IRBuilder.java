package Backend;

import AST.*;
import AST.symbol.arrayType;
import AST.symbol.classType;
import AST.symbol.funcSymbol;
import IR.Block;
import IR.Function;
import IR.IR;
import IR.inst.*;
import IR.operand.Void;
import IR.operand.*;
import IR.type.*;
import Util.error.internalError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IRBuilder implements ASTVisitor {
    public IR ir;
    public Block currentBlock = null;
    public Function currentFunction = null;
    public ClassType currentClass = null;
    public int loopDepth = 0;

    public IRBuilder(IR ir) {
        this.ir = ir;
    }

    public Register loadPtr(Operand p) {
        Register tmp = new Register(((Pointer) p.type).pointType, "tmp.");
        currentBlock.addInst(new Load(currentBlock, tmp, p));
        return tmp;
    }

    public Operand getReg(Operand a) {
        if (a.type instanceof Pointer) return loadPtr(a);
        else return a;
    }

    public void checkBranch(ExprNode it) {
        if (it.trueBlock == null) return;
        if (it.trueBlock.branchPhi != null) it.trueBlock.branchPhi.add(currentBlock, new ConstBool(true));
        if (it.falseBlock.branchPhi != null) it.falseBlock.branchPhi.add(currentBlock, new ConstBool(false));
        currentBlock.addTerminator(new Branch(currentBlock, getReg(it.operand), it.trueBlock, it.falseBlock));
    }

    public void assign(Operand l, Operand r) {
        if (r instanceof Null) {
            if (((Pointer) l.type).pointType instanceof Pointer)
                currentBlock.addInst(new Store(currentBlock, l, new Null(((Pointer) l.type).pointType)));
            else currentBlock.addInst(new Assign(currentBlock, l, r));
            return;
        }
        if (l instanceof Register) {
            if (l.type instanceof Pointer && l.type.equals(r.type)) {
                if (((Register) l).isConstPtr) {
                    r = getReg(r);
                    currentBlock.addInst(new Store(currentBlock, l, r));
                } else {
                    currentBlock.addInst(new Assign(currentBlock, l, r));
                }
            } else if (l.type instanceof Pointer && ((Pointer) l.type).pointType.equals(r.type)) {
                currentBlock.addInst(new Store(currentBlock, l, r));
            } else {
                r = getReg(r);
                currentBlock.addInst(new Assign(currentBlock, l, r));
            }
        } else {
            throw new internalError("assign");
        }
    }

    @Override
    public void visit(ProgramNode it) {
        it.body.forEach(x -> {
            if (x instanceof classDef) {
                currentClass = ((classDef) x).classType.classType;
                ((classDef) x).varList.forEach(t -> t.accept(this));
                currentClass = null;
            }
        });
        it.body.forEach(x -> x.accept(this));
    }

    @Override
    public void visit(TypeNode it) {

    }

    @Override
    public void visit(funcDef it) {
        currentFunction = it.func.func;
        if (currentClass != null) {
            currentFunction.name = currentClass.name + "." + it.name;
            currentClass.func.add(currentFunction);
            currentFunction.classPtr = new Register(new Pointer(currentClass), "this");
            currentFunction.params.add(currentFunction.classPtr);
        }
        ir.func.put(currentFunction.name, currentFunction);
        currentBlock = currentFunction.beginBlock;
        if (currentFunction.name.equals("main")) {
            currentBlock.addInst(new Call(currentBlock, null, ir.func.get("__gVar_init")));
        }
        it.paramList.forEach(x -> {
            x.varNode.operand = new Register(ir.getType(x.varNode.type), x.name);
            currentFunction.params.add(x.varNode.operand);
        });
        it.block.accept(this);
        if (!currentBlock.terminated) {
            Inst a;
            if (currentFunction.name.equals("main")) {
                a = new Return(currentBlock, new ConstInt(0, 32));
            } else if (currentFunction.returnType.equals(new VoidType())) {
                a = new Return(currentBlock, new Void());
            } else if (currentFunction.returnInst.size() == 0) {
                throw new internalError("return lost");
            } else {
                a = new Return(currentBlock, new Void());
            }
            currentBlock.addTerminator(a);
            currentFunction.returnInst.add(a);
        }
        if (currentFunction.returnInst.size() > 1) {
            currentBlock = new Block(loopDepth);
            if (!currentFunction.returnType.equals(new VoidType())) {
                Register tmp = new Register(currentFunction.returnType, "tmp.");
                Phi phi = new Phi(currentBlock, tmp);
                currentFunction.returnInst.forEach(x -> phi.add(x.block, ((Return) x.block.getTerminator()).value));
                currentBlock.addInst(phi);
                currentBlock.addTerminator(new Return(currentBlock, tmp));
            } else {
                currentBlock.addTerminator(new Return(currentBlock, new Void()));
            }
            currentFunction.returnInst.forEach(x -> {
                x.block.removeTerminator();
                x.block.addTerminator(new Jump(x.block, currentBlock));
            });
        }
        currentFunction = null;
    }

    @Override
    public void visit(classDef it) {
        currentClass = it.classType.classType;
        it.funcList.forEach(x -> x.accept(this));
        if (it.constructor != null) it.constructor.accept(this);
        ir.mxClass.put(currentClass.name, currentClass);
        currentClass = null;
    }

    @Override
    public void visit(blockStmt it) {
        for (StmtNode x : it.stmtList) {
            x.accept(this);
            if (currentBlock.terminated) break;
        }
    }

    @Override
    public void visit(varDefStmt it) {
        it.varList.forEach(x -> x.accept(this));
    }

    @Override
    public void visit(varDefSubStmt it) {
        BaseType type = ir.getType(it.varNode.type);
        if (it.varNode.isGlobal) {
            Register reg = new Register(new Pointer(type), it.name);
            reg.isGlobal = reg.isConstPtr = true;
            it.varNode.operand = reg;
            ir.gVar.put(it.name, reg);
            if (it.expr != null) {
                currentFunction = ir.func.get("__gVar_init");
                currentBlock = ir.__gVar_init_destBlock;
                it.expr.accept(this);
                assign(it.varNode.operand, it.expr.operand);
                ir.__gVar_init_destBlock = currentBlock;
                currentBlock = null;
                currentFunction = null;
            }
        } else {
            it.varNode.operand = new Register(type, it.name);
            if (currentClass != null && currentFunction == null) {
                it.varNode.isClassMember = true;
                ((Register) it.varNode.operand).name = currentClass.name + "." + it.name;
                currentClass.addVar((Register) it.varNode.operand);
            } else {
                if (it.expr != null) {
                    it.expr.accept(this);
                    assign(it.varNode.operand, it.expr.operand);
                }
            }
        }
    }

    @Override
    public void visit(ifStmt it) {
        Block trueBlock = new Block(loopDepth), falseBlock = new Block(loopDepth), destBlock = new Block(loopDepth);
        it.condition.trueBlock = trueBlock;
        if (it.falseStmt != null) it.condition.falseBlock = falseBlock;
        else it.condition.falseBlock = destBlock;
        it.condition.accept(this);
        currentBlock = trueBlock;
        it.trueStmt.accept(this);
        currentBlock.addTerminator(new Jump(currentBlock, destBlock));
        if (it.falseStmt != null) {
            currentBlock = falseBlock;
            it.falseStmt.accept(this);
            currentBlock.addTerminator(new Jump(currentBlock, destBlock));
        }
        currentBlock = destBlock;
    }

    @Override
    public void visit(whileStmt it) {
        loopDepth++;
        Block bodyBlock = new Block(loopDepth), condBlock = new Block(loopDepth), destBlock = new Block(loopDepth);
        it.destBlock = destBlock;
        it.condBlock = condBlock;
        currentBlock.addTerminator(new Jump(currentBlock, condBlock));
        currentBlock = condBlock;
        it.condition.trueBlock = bodyBlock;
        it.condition.falseBlock = destBlock;
        it.condition.accept(this);
        currentBlock = bodyBlock;
        it.body.accept(this);
        currentBlock.addTerminator(new Jump(currentBlock, condBlock));
        currentBlock = destBlock;
        loopDepth--;
    }

    @Override
    public void visit(forStmt it) {
        loopDepth++;
        Block bodyBlock = new Block(loopDepth), condBlock = new Block(loopDepth), destBlock = new Block(loopDepth), incBlock = new Block(loopDepth);
        it.destBlock = destBlock;
        it.incBlock = incBlock;
        if (it.init != null) it.init.accept(this);
        if (it.condition != null) {
            currentBlock.addTerminator(new Jump(currentBlock, condBlock));
            currentBlock = condBlock;
            it.condition.trueBlock = bodyBlock;
            it.condition.falseBlock = destBlock;
            it.condition.accept(this);
        } else {
            currentBlock.addTerminator(new Jump(currentBlock, bodyBlock));
            condBlock = bodyBlock;
        }
        currentBlock = bodyBlock;
        it.body.accept(this);
        currentBlock.addTerminator(new Jump(currentBlock, incBlock));
        currentBlock = incBlock;
        if (it.inc != null) it.inc.accept(this);
        currentBlock.addTerminator(new Jump(currentBlock, condBlock));
        currentBlock = destBlock;
        loopDepth--;
    }

    @Override
    public void visit(breakStmt it) {
        if (it.loop instanceof whileStmt) {
            currentBlock.addTerminator(new Jump(currentBlock, ((whileStmt) it.loop).destBlock));
        } else if (it.loop instanceof forStmt) {
            currentBlock.addTerminator(new Jump(currentBlock, ((forStmt) it.loop).destBlock));
        }
    }

    @Override
    public void visit(continueStmt it) {
        if (it.loop instanceof whileStmt) {
            currentBlock.addTerminator(new Jump(currentBlock, ((whileStmt) it.loop).condBlock));
        } else if (it.loop instanceof forStmt) {
            currentBlock.addTerminator(new Jump(currentBlock, ((forStmt) it.loop).incBlock));
        }
    }

    @Override
    public void visit(returnStmt it) {
        Inst a;
        if (it.returnValue != null) {
            it.returnValue.accept(this);
            if (it.returnValue.operand instanceof Register && ((Register) it.returnValue.operand).isConstPtr) {
                a = new Return(currentBlock, getReg(it.returnValue.operand));
            } else {
                a = new Return(currentBlock, it.returnValue.operand);
            }
        } else {
            a = new Return(currentBlock, new Void());
        }
        currentBlock.addTerminator(a);
        currentFunction.returnInst.add(a);
    }

    @Override
    public void visit(pureExprStmt it) {
        it.expr.accept(this);
    }

    @Override
    public void visit(emptyStmt it) {

    }

    @Override
    public void visit(varExpr it) {
        if (it.varNode.isClassMember) {
            ClassType classType = (ClassType) ((Pointer) currentFunction.classPtr.type).pointType;
            it.operand = new Register(new Pointer(classType.getVarReg(it.name).type), "tmp.");
            int offset = classType.getVarId(it.name);
            currentBlock.addInst(new GetElementPtr(currentBlock, it.operand, currentFunction.classPtr, new ConstInt(0, 32), new ConstInt(offset, 32)));
            ((Register) it.operand).isConstPtr = true;
        } else {
            it.operand = it.varNode.operand;
        }
        checkBranch(it);
    }

    @Override
    public void visit(subscriptExpr it) {
        it.base.accept(this);
        it.offset.accept(this);
        if (((Register) it.base.operand).isConstPtr) it.base.operand = getReg(it.base.operand);
        Register i_ptr = new Register(it.base.operand.type, "tmp.");
        currentBlock.addInst(new GetElementPtr(currentBlock, i_ptr, it.base.operand, getReg(it.offset.operand)));
        it.operand = i_ptr;
        ((Register) it.operand).isConstPtr = true;
        checkBranch(it);
    }

    @Override
    public void visit(funcCallExpr it) {
        Operand thisPtr = null;
        if (!(it.base instanceof varExpr)) {
            it.base.accept(this);
            if (((Register) it.base.operand).isConstPtr) thisPtr = getReg(it.base.operand);
            else thisPtr = it.base.operand;
        }
        if (it.base instanceof memberExpr && ((memberExpr) it.base).base.type instanceof arrayType) {
            Register bitPtr = new Register(new Pointer(new IntType(32)), "tmp.");
            Register sizePtr = new Register(new Pointer(new IntType(32)), "tmp.");
            it.operand = new Register(new IntType(32), "tmp.");
            currentBlock.addInst(new BitCast(currentBlock, bitPtr, thisPtr));
            currentBlock.addInst(new GetElementPtr(currentBlock, sizePtr, bitPtr, new ConstInt(-1, 32)));
            currentBlock.addInst(new Load(currentBlock, (Register) it.operand, sizePtr));
            return;
        }
        Function func = ((funcSymbol) it.base.type).func;
        it.operand = new Register(func.returnType, "tmp.");
        Call inst = new Call(currentBlock, (Register) it.operand, func);
        if (thisPtr != null) inst.param.add(thisPtr);
        if (it.base instanceof varExpr && func.inClass) inst.param.add(currentFunction.classPtr);
        it.exprList.forEach(x -> {
            x.accept(this);
            if (x.operand instanceof Register && ((Register) x.operand).isConstPtr) x.operand = getReg(x.operand);
            inst.param.add(x.operand);
        });
        currentBlock.addInst(inst);
        checkBranch(it);
    }

    @Override
    public void visit(exprListExpr it) {

    }

    @Override
    public void visit(memberExpr it) {
        it.base.accept(this);
        if (it.isFunc) {
            it.operand = it.base.operand;
        } else {
            Operand tmp = it.base.operand;
            if (((Register) tmp).isConstPtr) tmp = getReg(tmp);
            ClassType classType = (ClassType) ((Pointer) tmp.type).pointType;
            it.operand = new Register(new Pointer(classType.getVarReg(it.name).type), "tmp.");
            int offset = classType.getVarId(it.name);
            currentBlock.addInst(new GetElementPtr(currentBlock, it.operand, tmp, new ConstInt(0, 32), new ConstInt(offset, 32)));
            ((Register) it.operand).isConstPtr = true;
        }
        checkBranch(it);
    }

    public Register arrayNew(int dim, newExpr it, BaseType returnType) {
        BaseType itemType = ((Pointer) returnType).pointType;
        Register dataSize = new Register(new IntType(32), "tmp.");
        Register arraySize = new Register(new IntType(32), "tmp.");
        Register mallocPtr = new Register(new Pointer(new IntType(8)), "tmp.");
        Register sizePtr = new Register(new Pointer(new IntType(32)), "tmp.");
        Register arrayTPtr = new Register(new Pointer(new IntType(32)), "tmp.");

        Operand size = it.exprList.get(dim).operand;
        if (size instanceof Register && ((Register) size).isConstPtr) size = getReg(size);

        Register arrayPtr = new Register(returnType, "tmp.");
        currentBlock.addInst(new Binary(currentBlock, dataSize, "mul", size, new ConstInt(itemType.size() / 8, 32)));
        currentBlock.addInst(new Binary(currentBlock, arraySize, "add", dataSize, new ConstInt(32 / 8, 32)));
        Call inst = new Call(currentBlock, mallocPtr, ir.mallocFunc);
        inst.param.add(arraySize);
        currentBlock.addInst(inst);
        currentBlock.addInst(new BitCast(currentBlock, sizePtr, mallocPtr));
        currentBlock.addInst(new Store(currentBlock, sizePtr, size));
        currentBlock.addInst(new GetElementPtr(currentBlock, arrayTPtr, sizePtr, new ConstInt(1, 32)));
        currentBlock.addInst(new BitCast(currentBlock, arrayPtr, arrayTPtr));
        if (dim < it.exprList.size() - 1) {
            loopDepth++;
            Block bodyBlock = new Block(loopDepth), inc_cond_Block = new Block(loopDepth), destBlock = new Block(loopDepth);
            Register i = new Register(new IntType(32), "i");
            currentBlock.addInst(new Assign(currentBlock, i, new ConstInt(0, 32)));
            currentBlock.addTerminator(new Jump(currentBlock, bodyBlock));

            currentBlock = bodyBlock;
            Register i_ptr = new Register(returnType, "tmp.");
            currentBlock.addInst(new GetElementPtr(currentBlock, i_ptr, arrayPtr, i));
            Register i_item = arrayNew(dim + 1, it, itemType);
            currentBlock.addInst(new Store(currentBlock, i_ptr, i_item));
            currentBlock.addTerminator(new Jump(currentBlock, inc_cond_Block));

            currentBlock = inc_cond_Block;
            currentBlock.addInst(new Binary(currentBlock, i, "add", i, new ConstInt(1, 32)));
            Register cmp_tmp = new Register(new BoolType(), "tmp.");
            currentBlock.addInst(new Cmp(currentBlock, cmp_tmp, "slt", i, size));
            currentBlock.addTerminator(new Branch(currentBlock, cmp_tmp, bodyBlock, destBlock));

            currentBlock = destBlock;
            loopDepth--;
        }
        return arrayPtr;
    }

    @Override
    public void visit(newExpr it) {
        if (it.exprList != null) it.exprList.forEach(x -> x.accept(this));
        if (it.type instanceof arrayType) {
            it.operand = arrayNew(0, it, ir.getType(it.type));
        } else {
            Register mallocPtr = new Register(new Pointer(new IntType(8)), "tmp.");
            ClassType classType = ((classType) it.type).classType;
            it.operand = new Register(new Pointer(classType), "tmp.");
            Call inst = new Call(currentBlock, mallocPtr, ir.mallocFunc);
            inst.param.add(new ConstInt(classType.size() / 8, 32));
            currentBlock.addInst(inst);
            currentBlock.addInst(new BitCast(currentBlock, it.operand, mallocPtr));
            if (classType.constructor != null) {
                Call inst2 = new Call(currentBlock, null, classType.constructor);
                inst2.param.add(it.operand);
                currentBlock.addInst(inst2);
            }
        }
    }

    @Override
    public void visit(suffixExpr it) {
        it.src.accept(this);
        Operand src = getReg(it.src.operand);
        it.operand = new Register(src.type, "tmp.");
        assign(it.operand, src);
        Register tmp = new Register(src.type, "tmp.");
        switch (it.op) {
            case "++":
                currentBlock.addInst(new Binary(currentBlock, tmp, "add", src, new ConstInt(1, 32)));
                break;
            case "--":
                currentBlock.addInst(new Binary(currentBlock, tmp, "sub", src, new ConstInt(1, 32)));
                break;
            default:
                break;
        }
        assign(it.src.operand, tmp);
        checkBranch(it);
    }

    @Override
    public void visit(prefixExpr it) {
        it.src.accept(this);
        Operand src = getReg(it.src.operand);
        it.operand = new Register(src.type, "tmp.");
        switch (it.op) {
            case "++":
                currentBlock.addInst(new Binary(currentBlock, (Register) it.operand, "add", src, new ConstInt(1, 32)));
                assign(it.src.operand, it.operand);
                break;
            case "--":
                currentBlock.addInst(new Binary(currentBlock, (Register) it.operand, "sub", src, new ConstInt(1, 32)));
                assign(it.src.operand, it.operand);
                break;
            case "+":
                it.operand = src;
                break;
            case "-":
                currentBlock.addInst(new Binary(currentBlock, (Register) it.operand, "sub", new ConstInt(0, 32), src));
                break;
            case "~":
                currentBlock.addInst(new Binary(currentBlock, (Register) it.operand, "xor", src, new ConstInt(-1, 32)));
                break;
            case "!":
                currentBlock.addInst(new Binary(currentBlock, (Register) it.operand, "xor", src, new ConstBool(true)));
                break;
            default:
                break;
        }
        checkBranch(it);
    }

    @Override
    public void visit(binaryExpr it) {
        String op = null;
        String strOp = null;
        switch (it.op) {
            case "*":
                op = "mul";
                break;
            case "/":
                op = "sdiv";
                break;
            case "%":
                op = "srem";
                break;
            case "-":
                op = "sub";
                break;
            case "<<":
                op = "shl";
                break;
            case ">>":
                op = "ashr";
                break;
            case "&":
                op = "and";
                break;
            case "^":
                op = "xor";
                break;
            case "|":
                op = "or";
                break;
            case "+":
                op = "add";
                strOp = "__mx_builtin_str_add";
                break;
            case "<":
                op = "slt";
                strOp = "__mx_builtin_str_lt";
                break;
            case ">":
                op = "sgt";
                strOp = "__mx_builtin_str_gt";
                break;
            case "<=":
                op = "sle";
                strOp = "__mx_builtin_str_le";
                break;
            case ">=":
                op = "sge";
                strOp = "__mx_builtin_str_ge";
                break;
            case "==":
                op = "eq";
                strOp = "__mx_builtin_str_eq";
                break;
            case "!=":
                op = "ne";
                strOp = "__mx_builtin_str_ne";
                break;
            default:
                break;
        }
        switch (it.op) {
            case "*":
            case "/":
            case "%":
            case "-":
            case "<<":
            case ">>":
            case "&":
            case "^":
            case "|":
                it.src1.accept(this);
                it.src2.accept(this);
                Operand src1 = getReg(it.src1.operand);
                Operand src2 = getReg(it.src2.operand);
                it.operand = new Register(src1.type, "tmp.");
                currentBlock.addInst(new Binary(currentBlock, (Register) it.operand, op, src1, src2));
                break;
            case "+":
                if (it.src1.type.isString()) {
                    it.src1.accept(this);
                    it.src2.accept(this);
                    src1 = getReg(it.src1.operand);
                    src2 = getReg(it.src2.operand);
                    it.operand = new Register(src1.type, "tmp.");
                    Function func = new Function(strOp);
                    func.returnType = new StringType();
                    Call inst = new Call(currentBlock, (Register) it.operand, func);
                    inst.param.add(src1);
                    inst.param.add(src2);
                    currentBlock.addInst(inst);
                } else {
                    it.src1.accept(this);
                    it.src2.accept(this);
                    src1 = getReg(it.src1.operand);
                    src2 = getReg(it.src2.operand);
                    it.operand = new Register(src1.type, "tmp.");
                    currentBlock.addInst(new Binary(currentBlock, (Register) it.operand, op, src1, src2));
                }
                break;
            case "<":
            case ">":
            case "<=":
            case ">=":
                if (it.src1.type.isString()) {
                    it.src1.accept(this);
                    it.src2.accept(this);
                    src1 = getReg(it.src1.operand);
                    src2 = getReg(it.src2.operand);
                    it.operand = new Register(src1.type, "tmp.");
                    Function func = new Function(strOp);
                    func.returnType = new BoolType();
                    Call inst = new Call(currentBlock, (Register) it.operand, func);
                    inst.param.add(src1);
                    inst.param.add(src2);
                    currentBlock.addInst(inst);
                } else {
                    it.src1.accept(this);
                    it.src2.accept(this);
                    src1 = getReg(it.src1.operand);
                    src2 = getReg(it.src2.operand);
                    it.operand = new Register(new BoolType(), "tmp.");
                    currentBlock.addInst(new Cmp(currentBlock, (Register) it.operand, op, src1, src2));
                }
                checkBranch(it);
                break;
            case "&&":
                if (it.trueBlock != null) {
                    Block tmp = new Block(loopDepth);
                    it.src1.trueBlock = tmp;
                    it.src1.falseBlock = it.falseBlock;
                    it.src2.trueBlock = it.trueBlock;
                    it.src2.falseBlock = it.falseBlock;
                    it.src1.accept(this);
                    currentBlock = tmp;
                    it.src2.accept(this);
                } else {
                    Block tmp = new Block(loopDepth), dest = new Block(loopDepth);
                    it.operand = new Register(new BoolType(), "tmp.");
                    Phi phi = new Phi(dest, (Register) it.operand);
                    dest.branchPhi = phi;
                    it.src1.trueBlock = tmp;
                    it.src1.falseBlock = dest;
                    it.src1.accept(this);
                    currentBlock = tmp;
                    it.src2.accept(this);
                    phi.add(currentBlock, it.src2.operand);
                    currentBlock.addTerminator(new Jump(currentBlock, dest));
                    currentBlock = dest;
                    currentBlock.addInst(phi);
                }
                break;
            case "||":
                if (it.trueBlock != null) {
                    Block tmp = new Block(loopDepth);
                    it.src1.trueBlock = it.trueBlock;
                    it.src1.falseBlock = tmp;
                    it.src2.trueBlock = it.trueBlock;
                    it.src2.falseBlock = it.falseBlock;
                    it.src1.accept(this);
                    currentBlock = tmp;
                    it.src2.accept(this);
                } else {
                    Block tmp = new Block(loopDepth), dest = new Block(loopDepth);
                    it.operand = new Register(new BoolType(), "tmp.");
                    it.src1.trueBlock = dest;
                    it.src1.falseBlock = tmp;
                    it.src1.accept(this);
                    Block src1Block = currentBlock;
                    currentBlock = tmp;
                    it.src2.accept(this);
                    Block src2Block = currentBlock;
                    currentBlock.addTerminator(new Jump(currentBlock, dest));
                    currentBlock = dest;
                    Phi phi = new Phi(currentBlock, (Register) it.operand);
                    phi.add(src1Block, new ConstBool(true));
                    phi.add(src2Block, it.src2.operand);
                    currentBlock.addInst(phi);
                }
                break;
            case "==":
            case "!=":
                if (it.src1.type.isString()) {
                    it.src1.accept(this);
                    it.src2.accept(this);
                    src1 = getReg(it.src1.operand);
                    src2 = getReg(it.src2.operand);
                    it.operand = new Register(src1.type, "tmp.");
                    Function func = new Function(strOp);
                    func.returnType = new BoolType();
                    Call inst = new Call(currentBlock, (Register) it.operand, func);
                    inst.param.add(src1);
                    inst.param.add(src2);
                    currentBlock.addInst(inst);
                } else {
                    it.src1.accept(this);
                    it.src2.accept(this);
                    src1 = it.src1.operand;
                    if (src1 instanceof Register && ((Register) src1).isConstPtr) src1 = getReg(src1);
                    src2 = it.src2.operand;
                    if (src2 instanceof Register && ((Register) src2).isConstPtr) src2 = getReg(src2);
                    it.operand = new Register(new BoolType(), "tmp.");
                    currentBlock.addInst(new Cmp(currentBlock, (Register) it.operand, op, src1, src2));
                }
                checkBranch(it);
                break;
            case "=":
                it.src1.accept(this);
                it.src2.accept(this);
                it.operand = it.src1.operand;
                assign(it.src1.operand, it.src2.operand);
                break;
            default:
                break;
        }
    }

    @Override
    public void visit(thisExpr it) {
        it.operand = currentFunction.classPtr;
    }

    @Override
    public void visit(intLiteralExpr it) {
        it.operand = new ConstInt(it.value, 32);
    }

    @Override
    public void visit(boolLiteralExpr it) {
        it.operand = new ConstBool(it.value);
        checkBranch(it);
    }

    @Override
    public void visit(stringLiteralExpr it) {
        String name = "const_str_" + ir.constStr.size();
        String value = it.value.substring(1, it.value.length() - 1);
        it.operand = new ConstStr(name, value);
        ir.constStr.put(name, (ConstStr) it.operand);
    }

    @Override
    public void visit(nullLiteralExpr it) {
        it.operand = new Null();
    }

    public ArrayList<Block> rBlocks = new ArrayList<>();

    public void dfsBlock(Block block) {
        block.name = "block." + currentFunction.blocks.size();
        currentFunction.blocks.add(block);
        block.nxt.forEach(x -> {
            if (!currentFunction.blocks.contains(x)) dfsBlock(x);
        });
        rBlocks.add(0, block);
    }

    public void removeDeadBlock() {
        currentFunction.blocks.forEach(x -> {
            for (int i = 0; i < x.pre.size(); i++) {
                if (x.pre.get(i).name == null) {
                    x.pre.remove(i);
                    i--;
                }
            }
        });
    }

    public void doEachInst() {
        AtomicInteger tot = new AtomicInteger();
        currentFunction.params.forEach(x -> {
            if (x instanceof Register) {
                // collect params
                currentFunction.vars.add((Register) x);
            }
        });
        currentFunction.blocks.forEach(t -> t.inst.forEach(x -> {
            // remove dead block in phi
            if (x instanceof Phi) {
                for (int i = 0; i < ((Phi) x).blocks.size(); i++) {
                    if (!currentFunction.blocks.contains(((Phi) x).blocks.get(i))) {
                        ((Phi) x).blocks.remove(i);
                        ((Phi) x).values.remove(i);
                        i--;
                    }
                }
            }
            if (x.reg != null) {
                if (!x.reg.name.equals("tmp.")) {
                    // collect vars
                    currentFunction.vars.add(x.reg);
                    x.reg.assign.add(x);
                } else {
                    // tmp rename
                    x.reg.name = "tmp." + (tot.getAndIncrement());
                }
            }
        }));
    }

    public void renameSameNameVar() {
        ArrayList<Register> a = new ArrayList<>(currentFunction.vars);
        for (int i = 0; i < a.size(); i++)
            for (int j = i + 1; j < a.size(); j++)
                if (a.get(j).name.equals(a.get(i).name))
                    a.get(j).name = a.get(j).name + "_rename";
    }

    public HashMap<Block, Integer> dfn = new HashMap<>();
    public HashMap<Block, Block> iDom = new HashMap<>();
    public HashMap<Block, ArrayList<Block>> domSon = new HashMap<>();
    public HashMap<Block, ArrayList<Block>> domFr = new HashMap<>();

    public Block intersect(Block a, Block b) {
        if (a == null) return b;
        if (b == null) return a;
        while (a != b) {
            while (dfn.get(a) > dfn.get(b)) a = iDom.get(a);
            while (dfn.get(a) < dfn.get(b)) b = iDom.get(b);
        }
        return a;
    }

    public void domTree() {
        for (int i = 0; i < rBlocks.size(); i++) {
            dfn.put(rBlocks.get(i), i);
            iDom.put(rBlocks.get(i), null);
            domSon.put(rBlocks.get(i), new ArrayList<>());
        }
        iDom.replace(currentFunction.beginBlock, currentFunction.beginBlock);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 1; i < rBlocks.size(); i++) {
                Block new_iDom = null;
                for (int i1 = 0; i1 < rBlocks.get(i).pre.size(); i1++) {
                    if (iDom.get(rBlocks.get(i).pre.get(i1)) != null)
                        new_iDom = intersect(new_iDom, rBlocks.get(i).pre.get(i1));
                }
                if (iDom.get(rBlocks.get(i)) != new_iDom) {
                    iDom.replace(rBlocks.get(i), new_iDom);
                    changed = true;
                }
            }
        }
        iDom.forEach((x, f) -> {
            if (f != null && x != f) domSon.get(f).add(x);
        });
    }

    public void domFrontier() {
        rBlocks.forEach(x -> domFr.put(x, new ArrayList<>()));
        rBlocks.forEach(x -> {
            if (x.pre.size() >= 2) {
                x.pre.forEach(p -> {
                    Block r = p;
                    while (r != iDom.get(x)) {
                        domFr.get(r).add(x);
                        r = iDom.get(r);
                    }
                });
            }
        });
    }

    public void getPhi() {
        dfn = new HashMap<>();
        iDom = new HashMap<>();
        domSon = new HashMap<>();
        domFr = new HashMap<>();
        domTree();
        domFrontier();
        currentFunction.vars.forEach(x -> {
            HashSet<Block> have = new HashSet<>();
            for (int i = 0; i < x.assign.size(); i++) {
                Inst p = x.assign.get(i);
                domFr.get(p.block).forEach(b -> {
                    if (!have.contains(b)) {
                        Phi t = new Phi(b, x);
                        t.domPhi = true;
                        b.addInstFront(t);
                        x.assign.add(t);
                        have.add(b);
                    }
                });
            }
        });
    }

    public void renameVar(Register x, Block b) {
        Register ve = x.rename_stack.peek();
        b.inst.forEach(s -> {
            if (!(s instanceof Phi) || !((Phi) s).domPhi) {
                s.replace(x, x.rename_stack.peek());
            }
            if (s.reg != null && s.reg == x) {
                s.reg = new Register(x.type, x.name + "_" + (x.rename_id++));
                x.rename_stack.push(s.reg);
            }
        });
        b.nxt.forEach(s -> {
            s.inst.forEach(i -> {
                if (i instanceof Phi && ((Phi) i).phiReg == x) {
                    if (x.rename_stack.size() > 1) ((Phi) i).add(b, x.rename_stack.peek());
                    else ((Phi) i).add(b, x.type.getInit());
                }
            });
        });
        domSon.get(b).forEach(s -> renameVar(x, s));
        while (x.rename_stack.peek() != ve) x.rename_stack.pop();
    }

    public void simplifyPhi() {
        // size=1
        currentFunction.blocks.forEach(t -> {
            for (int i = 0; i < t.inst.size(); i++) {
                Inst x = t.inst.get(i);
                if (x instanceof Phi && ((Phi) x).values.size() == 1) {
                    t.inst.set(i, new Assign(t, x.reg, ((Phi) x).values.get(0)));
                }
            }
        });
        // phi in phi (in same block)
        currentFunction.blocks.forEach(t -> {
            HashMap<Register, Phi> phis = new HashMap<>();
            t.inst.forEach(x -> {
                if (x instanceof Phi) phis.put(x.reg, (Phi) x);
            });
            for (int i = 0; i < t.inst.size(); i++) {
                Inst x = t.inst.get(i);
                if (x instanceof Phi) {
                    for (int j = 0; j < ((Phi) x).values.size(); j++) {
                        if (((Phi) x).values.get(j) instanceof Register && phis.get((Register) ((Phi) x).values.get(j)) != null) {
                            Phi a = phis.get((Register) ((Phi) x).values.get(j));
                            for (int k = 0; k < a.values.size(); k++) {
                                if (a.blocks.get(k) == ((Phi) x).blocks.get(j)) {
                                    ((Phi) x).values.set(j, a.values.get(k));
                                }
                            }
                        }
                    }
                }
            }
        });
        // unused phi
        AtomicBoolean cond = new AtomicBoolean(true);
        while (cond.get()) {
            cond.set(false);
            currentFunction.blocks.forEach(t -> t.inst.forEach(x -> {
                ArrayList<Operand> ops = x.getUseOperand();
                ops.forEach(op -> {
                    if (op instanceof Register) {
                        ((Register) op).isUsed = true;
                    }
                });
            }));
            currentFunction.blocks.forEach(t -> {
                for (int i = 0; i < t.inst.size(); i++) {
                    Inst x = t.inst.get(i);
                    if (x instanceof Phi) {
                        if (!x.reg.isUsed) {
                            t.removeInst(x);
                            i--;
                            cond.set(true);
                        } else {
                            x.reg.isUsed = false;
                        }
                    }
                }
            });
        }
    }

    public void run() {
        ir.func.forEach((s, x) -> {
            currentFunction = x;
            ir.__gVar_init_destBlock.addTerminator(new Return(ir.__gVar_init_destBlock, new Void()));
            dfsBlock(x.beginBlock);
            removeDeadBlock();
            doEachInst();
            renameSameNameVar();
            getPhi();
            currentFunction.vars.forEach(v -> {
                v.rename_stack.push(new Register(v.type, v.name + "_" + (v.rename_id++)));
                for (int i = 0; i < currentFunction.params.size(); i++) {
                    if (currentFunction.params.get(i) == v) {
                        v.rename_stack.push(new Register(v.type, v.name + "_" + (v.rename_id++)));
                        currentFunction.params.set(i, v.rename_stack.peek());
                    }
                }
                renameVar(v, currentFunction.beginBlock);
            });
            simplifyPhi();
        });
    }
}

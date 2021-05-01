package Backend;

import IR.Block;
import IR.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DominatorTree {

    public Function currentFunction;

    public DominatorTree(Function func) {
        this.currentFunction = func;
    }

    public HashSet<Block> visited = new HashSet<>();
    public ArrayList<Block> rBlocks = new ArrayList<>();

    public void dfsBlock(Block block) {
        visited.add(block);
        block.nxt.forEach(x -> {
            if (!visited.contains(x)) dfsBlock(x);
        });
        rBlocks.add(0, block);
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

    public ArrayList<Block> rNodes = new ArrayList<>();
    public HashMap<Block, HashSet<Block>> domSubTree = new HashMap<>();

    public void dfsTree(Block x) {
        HashSet<Block> sub = new HashSet<>();
        domSon.get(x).forEach(a -> {
            dfsTree(a);
            sub.add(a);
            sub.addAll(domSubTree.get(a));
        });
        rNodes.add(x);
        domSubTree.put(x, sub);
    }

    public void run() {
        dfsBlock(currentFunction.beginBlock);
        domTree();
        domFrontier();
        dfsTree(currentFunction.beginBlock);
    }
}

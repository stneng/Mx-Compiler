package Backend.optimizer;

import IR.IR;

public class Optimizer {
    public IR ir;

    public Optimizer(IR ir) {
        this.ir = ir;
    }

    public void run() {
        for (int i = 0; i < 10; i++) {
            new CleanUp(ir).run();
            new SCCP(ir).run();
            new CleanUp(ir).run();
            new CSE(ir).run();
            new CleanUp(ir).run();
            new ADCE(ir).run();
            new Inline(ir).run();
            new CleanUp(ir).run();
            new LICM(ir).run();
            new CleanUp(ir).run();
            new ImmInstOpt(ir).run();
            new CleanUp(ir).run();
            new MemAccess(ir).run();
        }
        new CleanUp(ir).run();
    }
}

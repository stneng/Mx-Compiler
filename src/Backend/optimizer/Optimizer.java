package Backend.optimizer;

import IR.IR;

public class Optimizer {
    public IR ir;

    public Optimizer(IR ir) {
        this.ir = ir;
    }

    public void run() {
        new CleanUp(ir).run();
        new SCCP(ir).run();
        new CleanUp(ir).run();
        new ADCE(ir).run();
        new Inline(ir).run();
        new CleanUp(ir).run();
    }
}

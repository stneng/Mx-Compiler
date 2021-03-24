package IR.operand;

import IR.type.StringType;

public class ConstStr extends Operand {
    public String name;
    public String value;
    public String realValue;

    public ConstStr(String name, String value) {
        super(new StringType());
        this.name = name;
        this.value = value;
        this.realValue = value.replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"");
    }

    public String convert() {
        return value.replace("\\\\", "\\5C")
                .replace("\\n", "\\0A")
                .replace("\\r", "\\0D")
                .replace("\\t", "\\09")
                .replace("\\\"", "\\22");
    }

    @Override
    public String toString() {
        return "getelementptr inbounds ([ " + (realValue.length() + 1) + " x i8 ], [ " + (realValue.length() + 1) + " x i8 ]* @" + name + ", i32 0, i32 0)";
    }

    @Override
    public boolean equals(Operand t) {
        return this == t;
    }
}

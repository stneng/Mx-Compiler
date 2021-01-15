package Util.symbol;

public class varSymbol {
    public String name;
    public Type type;

    public varSymbol(String name) {
        this.name = name;
    }

    public varSymbol(String name, Type type) {
        this.name = name;
        this.type = type;
    }
}

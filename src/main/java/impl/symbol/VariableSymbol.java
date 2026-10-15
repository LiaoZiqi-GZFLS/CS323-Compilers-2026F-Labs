package impl.symbol;

import impl.Scope;
import impl.type.Type;

public class VariableSymbol implements Symbol{
    private final String name;
    private final Type type;

    public VariableSymbol(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", name, type);
    }
}

package impl.type;

public enum PrimitiveType implements Type {
    INT("int"), CHAR("char"), VOID("void");

    private final String name;

    private PrimitiveType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
package impl.symbol;

import impl.Scope;
import impl.type.Type;

public interface Symbol {
    String getName(); // 符号名
    Type getType();// 符号类型
}

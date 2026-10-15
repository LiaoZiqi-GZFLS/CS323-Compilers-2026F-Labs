package impl;

import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * 输出格式：  行号:列号: error: 错误种类 '标识符'
 *   - 行号：1 起（ANTLR Token.getLine()）
 *   - 列号：0 起（ANTLR Token.getCharPositionInLine()），即标识符首字符所在列
 */
public abstract class SplcSemanticError {

    /** 拼装并返回完整的错误行文本。 */
    public abstract String printBase();

    /** 未声明便使用。 */
    public static SplcSemanticError undeclaredUse(TerminalNode identifier) {
        return new UndeclaredUseError(identifier);
    }

    /** 同一作用域内变量（含形参）重复定义。 */
    public static SplcSemanticError redefinition(TerminalNode identifier) {
        return new RedefinitionError(identifier);
    }

    /** 同一作用域内函数重复声明。 */
    public static SplcSemanticError redeclaration(TerminalNode identifier) {
        return new RedeclarationError(identifier);
    }

    @Override
    public String toString() {
        return printBase();
    }

    private static class UndeclaredUseError extends SplcSemanticError {
        private final TerminalNode identifier;

        UndeclaredUseError(TerminalNode identifier) {
            this.identifier = identifier;
        }

        @Override
        public String printBase() {
            return String.format("%d:%d: error: Undeclared use of '%s'",
                    identifier.getSymbol().getLine(),
                    identifier.getSymbol().getCharPositionInLine(),
                    identifier.getText());
        }
    }

    private static class RedefinitionError extends SplcSemanticError {
        private final TerminalNode identifier;

        RedefinitionError(TerminalNode identifier) {
            this.identifier = identifier;
        }

        @Override
        public String printBase() {
            return String.format("%d:%d: error: Redefinition of '%s'",
                    identifier.getSymbol().getLine(),
                    identifier.getSymbol().getCharPositionInLine(),
                    identifier.getText());
        }
    }

    private static class RedeclarationError extends SplcSemanticError {
        private final TerminalNode identifier;

        RedeclarationError(TerminalNode identifier) {
            this.identifier = identifier;
        }

        @Override
        public String printBase() {
            return String.format("%d:%d: error: Redeclaration of '%s'",
                    identifier.getSymbol().getLine(),
                    identifier.getSymbol().getCharPositionInLine(),
                    identifier.getText());
        }
    }
}
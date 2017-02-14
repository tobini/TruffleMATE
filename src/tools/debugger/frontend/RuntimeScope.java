package tools.debugger.frontend;

import com.oracle.truffle.api.frame.MaterializedFrame;

import som.compiler.Variable;
import som.interpreter.LexicalScope;

public class RuntimeScope {
  private final MaterializedFrame frame;
  private final LexicalScope lexicalScope;

  public RuntimeScope(final MaterializedFrame frame, final LexicalScope lexcialScope) {
    this.frame = frame;
    this.lexicalScope = lexcialScope;
    assert frame.getFrameDescriptor() == lexcialScope.getFrameDescriptor();
  }

  public Variable[] getVariables() {
    return lexicalScope.getVariables();
  }

  public Object read(final Variable var) {
    return var.read(frame);
  }
}

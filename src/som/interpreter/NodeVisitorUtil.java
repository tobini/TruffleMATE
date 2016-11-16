package som.interpreter;

import som.interpreter.nodes.nary.ExpressionWithTagsNode;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeVisitor;


public final class NodeVisitorUtil {

  private static class DummyParent extends Node {
    private DummyParent() { super(); }
    @Child private ExpressionWithTagsNode child;

    private void adopt(final ExpressionWithTagsNode child) {
        this.child = insert(child);
    }
  }

  public static ExpressionWithTagsNode applyVisitor(final ExpressionWithTagsNode body,
      final NodeVisitor visitor) {
    DummyParent dummyParent = new DummyParent();
    dummyParent.adopt(body);
    body.accept(visitor);

    // need to return the child of the dummy parent,
    // since it could have been replaced
    return dummyParent.child;
  }
}

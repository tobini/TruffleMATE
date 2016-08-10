package som.primitives.arrays;


import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.vmobjects.SArray;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateNodeFactory
public abstract class ResizePrim extends BinaryExpressionNode {
  
  @Specialization
  public final Object doSArray(final SArray receiver, final long newSize) {
    //receiver.resize(newSize);
    return receiver;
  }
}

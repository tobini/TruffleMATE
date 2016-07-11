package som.primitives;

import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import tools.dym.Tags.StringAccess;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public class CharacterPrims {

  @GenerateNodeFactory
  public abstract static class NewCharPrim extends BinaryExpressionNode {
    @Specialization
    public final Character doCreate(final DynamicObject clazz, final long value) {
      return (char) value;
    }
    
    @Override
    protected boolean isTaggedWith(final Class<?> tag) {
      if (tag == StringAccess.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }
  
  @GenerateNodeFactory
  public abstract static class AsIntegerCharPrim extends UnaryExpressionNode {
    @Specialization
    public final long doCharacter(final char subject) {
      return (int) subject;
    }
    
    @Override
    protected boolean isTaggedWith(final Class<?> tag) {
      if (tag == StringAccess.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }
  
  @GenerateNodeFactory
  public abstract static class isDigitCharPrim extends UnaryExpressionNode {
    @Specialization
    public final boolean doCharacter(final char subject) {
      return Character.isDigit(subject);
    }
    
    @Override
    protected boolean isTaggedWith(final Class<?> tag) {
      if (tag == StringAccess.class) {
        return true;
      } else {
        return super.isTaggedWith(tag);
      }
    }
  }
}

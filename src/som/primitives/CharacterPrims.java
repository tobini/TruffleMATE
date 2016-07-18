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
  public abstract static class IsDigitCharPrim extends UnaryExpressionNode {
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
  
  @GenerateNodeFactory
  public abstract static class IsLetterCharPrim extends UnaryExpressionNode {
    @Specialization
    public final boolean doCharacter(final char subject) {
      return Character.isLetter(subject);
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
  public abstract static class IsAlphaNumericCharPrim extends UnaryExpressionNode {
    @Specialization
    public final boolean doCharacter(final char subject) {
      return Character.isLetterOrDigit(subject);
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
  public abstract static class AsUppercaseCharPrim extends UnaryExpressionNode {
    @Specialization
    public final char doCharacter(final char subject) {
      return Character.toUpperCase(subject);
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
  public abstract static class IsUppercaseCharPrim extends UnaryExpressionNode {
    @Specialization
    public final boolean doCharacter(final char subject) {
      return Character.isUpperCase(subject);
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
  public abstract static class AsLowercaseCharPrim extends UnaryExpressionNode {
    @Specialization
    public final char doCharacter(final char subject) {
      return Character.toLowerCase(subject);
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
  public abstract static class IsLowercaseCharPrim extends UnaryExpressionNode {
    @Specialization
    public final boolean doCharacter(final char subject) {
      return Character.isLowerCase(subject);
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
  public abstract static class CompareCharsPrim extends BinaryExpressionNode {
    @Specialization
    public final long doCharacter(final char receiver, final char param) {
      return Character.compare(receiver, param);
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

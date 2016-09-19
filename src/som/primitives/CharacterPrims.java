package som.primitives;

import som.interpreter.SomLanguage;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import tools.dym.Tags.StringAccess;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

public class CharacterPrims {

  @GenerateNodeFactory
  public abstract static class NewCharPrim extends BinaryExpressionNode {
    
    public NewCharPrim() {
      super(Source.newBuilder("NewChar").internal().name("new char").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

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
    public AsIntegerCharPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "As Integer For Characters"));
    }

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
    public IsDigitCharPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "IsDigit? for Chars"));
    }

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
  public abstract static class AsDigitCharPrim extends UnaryExpressionNode {
    public AsDigitCharPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "As Digit for Chars"));
    }

    @Specialization
    public final long doCharacter(final char subject) {
      return Character.getNumericValue(subject);
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
    public IsLetterCharPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Is Letter for Chars"));
    }

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
    public IsAlphaNumericCharPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "IsAlphaNumeric? for Chars"));
    }

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
    public AsUppercaseCharPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "AsUppercase for Characters"));
    }

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
    public IsUppercaseCharPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "IsUppercase? for Characters"));
    }

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
    public AsLowercaseCharPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "AsLowercase for Chars"));
    }

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
    public IsLowercaseCharPrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "IsLowercase? for Chars"));
    }

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
    public CompareCharsPrim() {
      super(Source.newBuilder("Compare Charactes").internal().name("compare chars").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

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
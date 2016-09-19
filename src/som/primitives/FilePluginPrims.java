package som.primitives;

import java.io.File;
import java.io.IOException;

import som.interpreter.SomLanguage;
import som.interpreter.nodes.ExpressionNode;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.nary.TernaryExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vm.Universe;
import som.vm.constants.Nil;
import som.vmobjects.SArray;
import som.vmobjects.SFile;
import som.vmobjects.SArray.ArrayType;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ValueProfile;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;


public abstract class FilePluginPrims {
  
  @GenerateNodeFactory
  public abstract static class ImageFilePrim extends UnaryExpressionNode {
    public ImageFilePrim() {
      super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Image File Name"));
    }

    @Specialization
    public String doGeneric(DynamicObject receiver) {
      return System.getProperty("user.dir") + "/" + Universe.getCurrent().imageName();
    }
  }
  
  @GenerateNodeFactory
  public abstract static class OpenFilePrim extends TernaryExpressionNode {
    @Specialization
    public Object doGeneric(DynamicObject receiver, String filename, Boolean writable) {
      SFile file = new SFile(new File(filename), writable);
      if (!file.getFile().exists()){
        return Nil.nilObject;
      }
      return file;
    }
  }
  
  @GenerateNodeFactory
  public abstract static class GetPositionFilePrim extends BinaryExpressionNode {
    
    public GetPositionFilePrim() {
      super(Source.newBuilder("Get Position in File").internal().name("get position").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

    @Specialization
    public long doGeneric(DynamicObject receiver, SFile file) {
      return file.getPosition();
    }
  }
  
  @GenerateNodeFactory
  public abstract static class SetPositionFilePrim extends TernaryExpressionNode {
    @Specialization
    @TruffleBoundary
    public long doGeneric(DynamicObject receiver, SFile file, long position) {
      file.setPosition(position);
      return position;
    }
  }
  
  @GenerateNodeFactory
  public abstract static class SizeFilePrim extends BinaryExpressionNode {
    public SizeFilePrim() {
      super(Source.newBuilder("Size for Files").internal().name("size file").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

    @Specialization
    public long doGeneric(DynamicObject receiver, SFile file) {
      return file.getFile().length();
    }
  }
  
  @GenerateNodeFactory
  @NodeChildren({
    @NodeChild(value = "receiver", type = ExpressionNode.class),
    @NodeChild(value = "sfile", type = ExpressionNode.class),
    @NodeChild(value = "vector", type = ExpressionNode.class),
    @NodeChild(value = "starting", type = ExpressionNode.class),
    @NodeChild(value = "count", type = ExpressionNode.class),
  })
  @ImportStatic(ArrayType.class)
  public abstract static class ReadIntoFilePrim extends ExpressionNode {
    public ReadIntoFilePrim() { super(SourceSection.createUnavailable(SomLanguage.PRIMITIVE_SOURCE_IDENTIFIER, "Read Into File")); }
    
    private final ValueProfile storageType = ValueProfile.createClassProfile();
    
    @Specialization(guards = {"isByteType(collection)"})
    public long doEmptyBytes(DynamicObject receiver, SFile file, SArray collection, long startingAt, long count) {
      if (ArrayType.isEmptyType(collection)){
        collection.transitionTo(ArrayType.BYTE, new byte[(int) count]);
      }
      byte[] buffer = collection.getByteStorage(storageType);
      return read(file, buffer, (int)startingAt - 1, (int)count);
    }
    
    @TruffleBoundary
    @Specialization(guards = {"!isByteType(collection)"})
    public long doEmpty(DynamicObject receiver, SFile file, SArray collection, long startingAt, long count) {
      byte[] buffer = new byte[(int) count];
      long countRead = read(file, buffer, (int)startingAt - 1, (int)count);
      /*TODO: Workaround this so in case the read is in a subpart of the array we do not lose the rest*/
      collection.transitionTo(ArrayType.CHAR, (new String(buffer)).toCharArray());
      return countRead;
    }
    
    @TruffleBoundary
    private static long read(SFile file, byte[] buffer, int start, int count){
      try {
        return (long) file.getInputStream().read(buffer, start, (int)count);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return 0;
    }
  }
  
  @GenerateNodeFactory
  public abstract static class AtEndFilePrim extends BinaryExpressionNode {
    public AtEndFilePrim() {
      super(Source.newBuilder("At End for Files").internal().name("at end").mimeType(SomLanguage.MIME_TYPE).build().createSection(null, 1));
    }

    @Specialization
    @TruffleBoundary
    public boolean doGeneric(DynamicObject receiver, SFile file) {
      try {
        return file.getInputStream().available() == 0;
      } catch (IOException e) {
        return true;
      }
    }
  }
}


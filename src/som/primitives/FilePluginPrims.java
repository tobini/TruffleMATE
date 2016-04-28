package som.primitives;

import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vm.Universe;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;


public abstract class FilePluginPrims {
  
  @GenerateNodeFactory
  public abstract static class ImageFilePrim extends UnaryExpressionNode {
    
    @Specialization
    public String doGeneric(DynamicObject receiver) {
      return System.getProperty("user.dir") + "/" + Universe.current().imageName();
    }
  }
}


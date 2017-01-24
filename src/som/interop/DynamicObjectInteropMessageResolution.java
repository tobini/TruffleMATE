/*Created by Smarr*/
package som.interop;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import som.interop.ValueConversion.ToSomConversion;
import som.interop.ValueConversionFactory.ToSomConversionNodeGen;
import som.interpreter.SomLanguage;
import som.interpreter.nodes.dispatch.GenericDispatchNode;
import som.vm.Universe;
import som.vm.constants.ExecutionLevel;
import som.vm.constants.Nil;


@MessageResolution(receiverType = DynamicObject.class, language = SomLanguage.class)
public class DynamicObjectInteropMessageResolution {

  @Resolve(message = "INVOKE")
  public abstract static class SObjectInvokeNode extends Node {
    @Child protected ToSomConversion convert = ToSomConversionNodeGen.create(null);

    protected Object access(final VirtualFrame frame, final DynamicObject rcvr,
        final String name, final Object[] args) {
      Object[] arguments = ValueConversion.convertToArgArray(convert, rcvr, args);

      Object result = new GenericDispatchNode(Universe.emptySource.createSection(1),
          Universe.getCurrent().symbolFor(name)).
          executeDispatch(frame, Nil.nilObject, ExecutionLevel.Base, arguments);

      if (result == Nil.nilObject) {
        return null;
      } else {
        return result;
      }
    }
  }
  
  @Resolve(message = "IS_BOXED")
  public abstract static class DynamicObjectIsBoxedNode extends Node {
    protected Object access(final VirtualFrame frame, final DynamicObject rcvr) {
      //TODO: Check what'd be the proper answer
      return false;
    }
  }

  @CanResolve
  public abstract static class CheckDynamicObject extends Node {
    protected static boolean test(final TruffleObject receiver) {
      return receiver instanceof DynamicObject;
    }
  }
}
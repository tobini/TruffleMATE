/*Created by Smarr*/
package som.interop;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;

import som.interop.ValueConversion.ToSomConversion;
import som.interop.ValueConversionFactory.ToSomConversionNodeGen;
import som.interpreter.SomLanguage;
import som.interpreter.nodes.dispatch.GenericBlockDispatchNode;
import som.vm.Universe;
import som.vm.constants.ExecutionLevel;
import som.vm.constants.Nil;
import som.vmobjects.SBlock;


@MessageResolution(receiverType = SBlock.class, language = SomLanguage.class)
public class SBlockInteropMessageResolution {

  @Resolve(message = "EXECUTE")
  public abstract static class SBlockExecuteNode extends Node {

    @Child protected GenericBlockDispatchNode block = new GenericBlockDispatchNode(Universe.emptySource.createSection(1));
    @Child protected ToSomConversion convert = ToSomConversionNodeGen.create(null);

    public Object access(final VirtualFrame frame, final SBlock rcvr,
        final Object[] args) {
      Object[] arguments = ValueConversion.convertToArgArray(convert, rcvr, args);
      Object result = block.executeDispatch(frame, Nil.nilObject,
          ExecutionLevel.Base, arguments);
      if (result == Nil.nilObject) {
        return null;
      } else {
        return result;
      }
    }
  }

  @CanResolve
  public abstract static class CheckSBlock extends Node {
    protected static boolean test(final TruffleObject receiver) {
      return receiver instanceof SBlock;
    }
  }
}

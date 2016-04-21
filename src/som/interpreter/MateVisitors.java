package som.interpreter;

import som.vm.constants.ExecutionLevel;

import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstanceVisitor;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;


public class MateVisitors {

  public static class FindFirstBaseLevelFrame implements FrameInstanceVisitor<FrameInstance>{

    @Override
    public FrameInstance visitFrame(FrameInstance frameInstance) {
      if (SArguments.getExecutionLevel(frameInstance.getFrame(FrameAccess.MATERIALIZE, true)) == ExecutionLevel.Base){
        return frameInstance;
      }
      return null;
    }
  }
}

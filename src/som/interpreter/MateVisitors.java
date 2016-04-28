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
  
  public static class FindSenderFrame implements FrameInstanceVisitor<FrameInstance>{
    private final FrameInstance current;
    private Boolean currentFound;
    public FindSenderFrame(FrameInstance frame){
      current = frame;
      currentFound = false;
    }
    
    @Override
    public FrameInstance visitFrame(FrameInstance frameInstance) {
      if (currentFound) return frameInstance;
      if (frameInstance.getFrame(FrameAccess.READ_ONLY, false) == current.getFrame(FrameAccess.READ_ONLY, false)){
        currentFound = true;
      }
      return null;
    }
  }
}

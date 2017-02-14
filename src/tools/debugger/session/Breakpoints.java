package tools.debugger.session;

import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.debug.Breakpoint;
import com.oracle.truffle.api.debug.Debugger;
import com.oracle.truffle.api.debug.DebuggerSession;
import tools.SourceCoordinate.FullSourceCoordinate;
import tools.debugger.WebDebugger;


public class Breakpoints {

  private final DebuggerSession debuggerSession;

  /**
   * Breakpoints directly managed by Truffle.
   */
  private final Map<BreakpointInfo, Breakpoint> truffleBreakpoints;

  /**
   * MessageReceiverBreakpoints, manually managed by us (instead of Truffle).
   */
  private final Map<FullSourceCoordinate, BreakpointEnabling<MessageReceiverBreakpoint>> receiverBreakpoints;


  public Breakpoints(final Debugger debugger, final WebDebugger webDebugger) {
    this.truffleBreakpoints = new HashMap<>();
    this.receiverBreakpoints = new HashMap<>();
    this.debuggerSession = debugger.startSession(webDebugger);
  }

  /*public void doSuspend(final MaterializedFrame frame, final SteppingLocation steppingLocation) {
    debuggerSession.doSuspend(frame, steppingLocation);
  }*/

  public synchronized void addOrUpdate(final LineBreakpoint bId) {
    Breakpoint bp = truffleBreakpoints.get(bId);
    if (bp == null) {
      WebDebugger.log("LineBreakpoint: " + bId);
      bp = Breakpoint.newBuilder(bId.getURI()).
          lineIs(bId.getLine()).
          build();
      debuggerSession.install(bp);
      truffleBreakpoints.put(bId, bp);
    }
    bp.setEnabled(bId.isEnabled());
  }

  public synchronized void addOrUpdate(final MessageSenderBreakpoint bId) {
    // saveTruffleBasedBreakpoints(bId, EventualMessageSend.class);
  }

  public synchronized void addOrUpdate(final MessageReceiverBreakpoint bId) {
    saveBreakpoint(bId, receiverBreakpoints);
  }

  private Breakpoint saveTruffleBasedBreakpoints(final SectionBreakpoint bId, final Class<?> tag) {
    Breakpoint bp = truffleBreakpoints.get(bId);
    if (bp == null) {
      bp = Breakpoint.newBuilder(bId.getCoordinate().uri).
          lineIs(bId.getCoordinate().startLine).
          // columnIs(bId.getCoordinate().startColumn).
          // sectionLength(bId.getCoordinate().charLength).
          // tag(tag).
          build();
      debuggerSession.install(bp);
      truffleBreakpoints.put(bId, bp);
    }
    bp.setEnabled(bId.isEnabled());
    return bp;
  }

  private <T extends SectionBreakpoint> void saveBreakpoint(final T bId,
      final Map<FullSourceCoordinate, BreakpointEnabling<T>> breakpoints) {
    FullSourceCoordinate coord = bId.getCoordinate();
    BreakpointEnabling<T> existingBP = breakpoints.get(coord);
    if (existingBP == null) {
      existingBP = new BreakpointEnabling<T>(bId);
      breakpoints.put(coord, existingBP);
    } else {
      existingBP.setEnabled(bId.isEnabled());
    }
  }

   public synchronized BreakpointEnabling<MessageReceiverBreakpoint> getReceiverBreakpoint(
      final FullSourceCoordinate section) {
    return receiverBreakpoints.computeIfAbsent(section,
        ss -> new BreakpointEnabling<>(new MessageReceiverBreakpoint(false, section)));
  }
}

package som.interpreter;

import java.io.IOException;

import som.vm.NotYetImplementedException;
import som.vm.Universe;
import tools.dym.Tags.ArrayRead;
import tools.dym.Tags.ArrayWrite;
import tools.dym.Tags.BasicPrimitiveOperation;
import tools.dym.Tags.CachedClosureInvoke;
import tools.dym.Tags.CachedVirtualInvoke;
import tools.dym.Tags.ClassRead;
import tools.dym.Tags.ComplexPrimitiveOperation;
import tools.dym.Tags.ControlFlowCondition;
import tools.dym.Tags.FieldRead;
import tools.dym.Tags.FieldWrite;
import tools.dym.Tags.LocalArgRead;
import tools.dym.Tags.LocalVarRead;
import tools.dym.Tags.LocalVarWrite;
import tools.dym.Tags.LoopBody;
import tools.dym.Tags.NewArray;
import tools.dym.Tags.NewObject;
import tools.dym.Tags.OpArithmetic;
import tools.dym.Tags.OpClosureApplication;
import tools.dym.Tags.OpComparison;
import tools.dym.Tags.OpLength;
import tools.dym.Tags.PrimitiveArgument;
import tools.dym.Tags.StringAccess;
import tools.dym.Tags.UnspecifiedInvoke;
import tools.dym.Tags.VirtualInvoke;
import tools.dym.Tags.VirtualInvokeReceiver;
import tools.highlight.Tags.ArgumentTag;
import tools.highlight.Tags.CommentTag;
import tools.highlight.Tags.DelimiterClosingTag;
import tools.highlight.Tags.DelimiterOpeningTag;
import tools.highlight.Tags.IdentifierTag;
import tools.highlight.Tags.KeywordTag;
import tools.highlight.Tags.LiteralTag;
import tools.highlight.Tags.LocalVariableTag;
import tools.highlight.Tags.StatementSeparatorTag;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags.CallTag;
import com.oracle.truffle.api.instrumentation.StandardTags.RootTag;
import com.oracle.truffle.api.instrumentation.StandardTags.StatementTag;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;

@TruffleLanguage.Registration(name = "TruffleMate", version = "0.1.0", mimeType = SomLanguage.MIME_TYPE)
 @ProvidedTags({RootTag.class, StatementTag.class, CallTag.class,
  KeywordTag.class, LiteralTag.class,
  CommentTag.class, IdentifierTag.class, ArgumentTag.class,
  LocalVariableTag.class, StatementSeparatorTag.class,
  DelimiterOpeningTag.class, DelimiterClosingTag.class,

  UnspecifiedInvoke.class, CachedVirtualInvoke.class,
  CachedClosureInvoke.class, VirtualInvoke.class,
  VirtualInvokeReceiver.class, NewObject.class, NewArray.class,
  ControlFlowCondition.class, FieldRead.class, FieldWrite.class, ClassRead.class,
  LocalVarRead.class, LocalVarWrite.class, LocalArgRead.class, ArrayRead.class,
  ArrayWrite.class, LoopNode.class, LoopBody.class, BasicPrimitiveOperation.class,
  ComplexPrimitiveOperation.class, PrimitiveArgument.class,
  StringAccess.class, OpClosureApplication.class, OpArithmetic.class,
  OpComparison.class, OpLength.class
})
public class SomLanguage extends TruffleLanguage<Universe> {

  public static final String MIME_TYPE = "application/x-mate-som";
  public static final String CMD_ARGS  = "command-line-arguments";
  public static final String FILE_EXTENSION = "som";
  public static final String DOT_FILE_EXTENSION = "." + FILE_EXTENSION;

  public static final SomLanguage INSTANCE = new SomLanguage();
  //public static final Source START = getSyntheticSource("", "START");

  public static Source getSyntheticSource(final String text, final String name) {
    return Source.newBuilder(text).internal().name(name).mimeType(SomLanguage.MIME_TYPE).build();
  }
  
  @Override
  protected Universe createContext(final Env env) {
    Universe vm;
    try {
      vm = new Universe((String[]) env.getConfig().get(CMD_ARGS));
    } catch (IOException e) {
      throw new RuntimeException("Failed accessing kernel or platform code of SOMns.", e);
    }
    return vm;
  }
  
  @Override
  protected CallTarget parse(final Source code, final Node context,
      final String... argumentNames) throws IOException {
    throw new NotYetImplementedException();
  }

  @Override
  protected Object findExportedSymbol(final Universe context,
      final String globalName, final boolean onlyExplicit) {
    throw new NotYetImplementedException();
  }

  @Override
  protected Object getLanguageGlobal(final Universe context) {
    return null;
  }

  @Override
  protected boolean isObjectOfLanguage(final Object object) {
    throw new NotYetImplementedException();
  }

  @Override
  protected boolean isInstrumentable(final Node node) {
    throw new NotYetImplementedException();
  }

  @Override
  protected Object evalInContext(final Source source, final Node node,
      final MaterializedFrame mFrame) throws IOException {
    throw new NotYetImplementedException();
  }
}

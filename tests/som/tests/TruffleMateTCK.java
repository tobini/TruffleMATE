package som.tests;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import som.interpreter.SomLanguage;
import som.vm.Universe;

import com.oracle.truffle.api.impl.FindContextNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;
import com.oracle.truffle.tck.TruffleTCK;


public class TruffleMateTCK extends TruffleTCK {
  
  @Override
  protected PolyglotEngine prepareVM(final PolyglotEngine.Builder preparedBuilder) throws Exception {
    preparedBuilder.config(SomLanguage.MIME_TYPE, SomLanguage.CMD_ARGS, new String [] {"-cp", "Smalltalk:"});
    //preparedBuilder.config(SomLanguage.MIME_TYPE, SomLanguage.AVOID_EXIT, true);
    //preparedBuilder.config(SomLanguage.MIME_TYPE, SomLanguage.CMD_ARGS, arguments);
    
    
    //String fname = vm.resolveClassFilePath("TruffleMateTCK");  
    URL filepath = getClass().getResource("TruffleMateTCK.som");
    Source source = Source.newBuilder(new File(filepath.getPath())).mimeType(
        mimeType()).name("TruffleMateTCK").build();
    PolyglotEngine engine = preparedBuilder.build();
    Value klass = engine.eval(source);
    //SClass tck = tckModule.as(SClass.class);

    //FindContextNode<Universe> contextNode = SomLanguage.INSTANCE.createNewFindContextNode();
    //Universe vm = contextNode.executeFindContext();

    //CompletableFuture<Object> future = new CompletableFuture<>();
    //vm.setCompletionFuture(future);

    //tck.getMixinDefinition().instantiateObject(tck, vm.getVmMirror());
    return engine;
  }
  
  @Override
  protected String mimeType() {
    return SomLanguage.MIME_TYPE;
  }

  @Override
  protected String fourtyTwo() {
    return "fourtytwo";
  }

  @Override
  protected String returnsNull() {
    return "returnNil";
  }

  @Override
  protected String applyNumbers() {
    return "apply:";
  }

  @Override
  protected String countInvocations() {
    return "countInvocation";
  }

  @Override
  protected String invalidCode() {
    return "this.foo() is more javaish than Smalltalk";
  }
  
  @Override
  protected String plusInt() {
    return "sum:and:";
  }
}
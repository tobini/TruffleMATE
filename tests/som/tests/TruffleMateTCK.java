package som.tests;

import java.io.File;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;

import som.interpreter.SomLanguage;
import som.vmobjects.SClass;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;
import com.oracle.truffle.tck.TruffleTCK;


public class TruffleMateTCK extends TruffleTCK {

  @Override
  protected PolyglotEngine prepareVM(final PolyglotEngine.Builder preparedBuilder) throws Exception {
    preparedBuilder.config(SomLanguage.MIME_TYPE, SomLanguage.CMD_ARGS, new String [] {"-cp", "Smalltalk:TestSuite/TruffleTCK:"});
    // preparedBuilder.config(SomLanguage.MIME_TYPE, SomLanguage.AVOID_EXIT, true);
    // preparedBuilder.config(SomLanguage.MIME_TYPE, SomLanguage.CMD_ARGS, arguments);
    // String fname = vm.resolveClassFilePath("TruffleMateTCK");  
    URL filepath = getClass().getResource("TruffleMateTCK.som");
    Source source = Source.newBuilder(new File(filepath.getPath())).mimeType(
        mimeType()).name("TruffleMateTCK").build();
    PolyglotEngine engine = preparedBuilder.build();
    Value value = engine.eval(source);
    DynamicObject klass = value.as(DynamicObject.class);
    SClass.initialize(klass);
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
  protected String identity() {
    return "identity:";
  }

  @Override
  protected String invalidCode() {
    return "this.foo() is more javaish than Smalltalk";
  }

  @Override
  protected String plusInt() {
    return "sum:and:";
  }

  @Override
  protected String compoundObject() {
    return "compoundObject";
  }

  @Override
  protected String globalObject() {
    return null;
  }

  @Override
  protected String valuesObject() {
    return "valuesObject";
  }

  @Override
  protected String objectWithElement() {
    return "objectWithElement";
  }

  @Override
  protected String functionAddNumbers() {
    return "functionAddNumbers";
  }

  @Override
  protected String complexAdd() {
    return "complexAdd";
  }

  @Override
  protected String complexAddWithMethod() {
    return "complexAddWithMethod";
  }

  @Override
  @Test
  @Ignore("todo: remove override")
  public void testCoExistanceOfMultipleLanguageInstances() throws Exception { }

  @Override
  @Test
  @Ignore("needs eval in language. don't want that")
  public void testEvaluateSource() throws Exception { }

  @Override
  @Test
  @Ignore("needs support for code snippet parsing, don't have that yet")
  public void multiplyTwoVariables() throws Exception { }

  @Override
  @Test
  @Ignore("todo: remove override")
  public void testSumRealOfComplexNumbersA() throws Exception { }

  @Override
  @Test
  @Ignore("todo: remove override")
  public void testSumRealOfComplexNumbersB() throws Exception { }

  @Override
  @Test
  @Ignore("todo: remove override")
  public void testSumRealOfComplexNumbersAsStructuredDataRowBased() throws Exception { }

  @Override
  @Test
  @Ignore("todo: remove override")
  public void testSumRealOfComplexNumbersAsStructuredDataColumnBased() throws Exception { }

  @Override
  @Test
  @Ignore("todo: remove override")
  public void testCopyComplexNumbersA() throws Exception {  }

  @Override
  @Test
  @Ignore("todo: remove override")
  public void testCopyComplexNumbersB() throws Exception { }

  @Override
  @Test
  @Ignore("todo: remove override")
  public void testCopyStructuredComplexToComplexNumbersA() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void timeOutTest() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void addOneToAnArrayElement() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testRootNodeName() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testFunctionAddNumbers() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testReadValueFromForeign() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testReadElementFromForeign() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testWriteValueToForeign() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testObjectWithValueAndAddProperty() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testIsExecutableOfForeign() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testCallMethod() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testHasSize() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testHasSizeOfForeign() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testGetSize() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testIsExecutable() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testWriteElementOfForeign() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testIsNullOfForeign() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testReadFromObjectWithElement() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testGetSizeOfForeign() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testIsNotNull() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testWriteToObjectWithValueProperty() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testReadFromObjectWithValueProperty() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testCallFunction() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testWriteToObjectWithElement() throws Exception { }

  @Test
  @Override
  @Ignore("todo: remove override")
  public void testPropertiesInteropMessage() throws Exception { }
}

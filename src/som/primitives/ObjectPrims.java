package som.primitives;

import som.interpreter.Types;
import som.interpreter.nodes.nary.BinaryExpressionNode;
import som.interpreter.nodes.nary.TernaryExpressionNode;
import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.primitives.reflection.IndexDispatch;
import som.vm.Universe;
import som.vm.constants.Globals;
import som.vm.constants.Nil;
import som.vm.constants.ReflectiveOp;
import som.vmobjects.SClass;
import som.vmobjects.SObject;
import som.vmobjects.SReflectiveObject;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

public final class ObjectPrims {

  @GenerateNodeFactory
  @Primitive(klass = "Object", selector = "instVarAt:")
  public abstract static class InstVarAtPrim extends BinaryExpressionNode {

    @Child private IndexDispatch dispatch;

    public InstVarAtPrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
      dispatch = IndexDispatch.create();
    }

    @Specialization
    public final Object doSObject(final DynamicObject receiver, final long idx) {
      return dispatch.executeDispatch(receiver, (int) idx - 1);
    }

    @Override
    public final Object executeEvaluated(final VirtualFrame frame,
      final Object receiver, final Object firstArg) {
      assert receiver instanceof DynamicObject;
      assert firstArg instanceof Long;

      DynamicObject rcvr = (DynamicObject) receiver;
      long idx     = (long) firstArg;
      return doSObject(rcvr, idx);
    }

    public ReflectiveOp reflectiveOperation() {
      return ReflectiveOp.LayoutPrimReadField;
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Object", selector = "instVarAt:put:", noWrapper = true)
  public abstract static class InstVarAtPutPrim extends TernaryExpressionNode {
    @Child private IndexDispatch dispatch;

    public InstVarAtPutPrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
      dispatch = IndexDispatch.create();
    }

    @Specialization
    public final Object doSObject(final DynamicObject receiver, final long idx, final Object val) {
      dispatch.executeDispatch(receiver, (int) idx - 1, val);
      return val;
    }

    @Override
    public final Object executeEvaluated(final VirtualFrame frame,
      final Object receiver, final Object firstArg, final Object secondArg) {
      assert receiver instanceof DynamicObject;
      assert firstArg instanceof Long;
      assert secondArg != null;

      DynamicObject rcvr = (DynamicObject) receiver;
      long idx     = (long) firstArg;
      return doSObject(rcvr, idx, secondArg);
    }

    public ReflectiveOp reflectiveOperation() {
      return ReflectiveOp.LayoutPrimWriteField;
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Object", selector = "instVarNamed:")
  public abstract static class InstVarNamedPrim extends BinaryExpressionNode {
    public InstVarNamedPrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
    }

    @TruffleBoundary
    @Specialization
    public final Object doSObject(final DynamicObject receiver, final SSymbol fieldName) {
      // CompilerAsserts.neverPartOfCompilation("InstVarNamedPrim");
      return receiver.get(SClass.lookupFieldIndex(SObject.getSOMClass(receiver), fieldName), Nil.nilObject);
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Object", selector = "halt", eagerSpecializable = false)
  public abstract static class HaltPrim extends UnaryExpressionNode {
    public HaltPrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization
    public final Object doSAbstractObject(final Object receiver) {
      Universe.errorPrintln("BREAKPOINT");
      return receiver;
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Object", selector = "class", eagerSpecializable = false)
  public abstract static class ClassPrim extends UnaryExpressionNode {
    public ClassPrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization
    public final DynamicObject doDynamicObject(final DynamicObject receiver) {
      return SObject.getSOMClass(receiver);
    }

    @Specialization
    public final DynamicObject doObject(final Object receiver) {
      return Types.getClassOf(receiver);
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Object", selector = "installEnvironment:", mate = true)
  public abstract static class InstallEnvironmentPrim extends BinaryExpressionNode {
    public InstallEnvironmentPrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
    }

    @Specialization(guards = "receiverIsSystemObject(receiver)")
    public final DynamicObject doSystemObject(final DynamicObject receiver, final DynamicObject environment) {
      Universe.getCurrent().setGlobalEnvironment(environment);
      return environment;
    }

    @Specialization
    public final Object doSObject(final DynamicObject receiver, final DynamicObject environment) {
      SReflectiveObject.setEnvironment(receiver, environment);
      return receiver;
    }

    public static final boolean receiverIsSystemObject(final DynamicObject receiver) {
      return receiver == Globals.systemObject;
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Object", selector = "shallowCopy", eagerSpecializable = false)
  public abstract static class ShallowCopyPrim extends UnaryExpressionNode {
    public ShallowCopyPrim(final boolean eagWrap, final SourceSection source) {
      super(false, source);
    }

    @Specialization
    public final Object doSObject(final DynamicObject receiver) {
      return receiver.copy(receiver.getShape());
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Object", selector = "hashcode", eagerSpecializable = false)
  @Primitive(klass = "Object", selector = "identityHash", eagerSpecializable = false)
  public abstract static class HashPrim extends UnaryExpressionNode {
    public HashPrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization
    @TruffleBoundary
    public final long doString(final String receiver) {
      return receiver.hashCode();
    }

    @Specialization
    @TruffleBoundary
    public final long doSSymbol(final SSymbol receiver) {
      return receiver.getString().hashCode();
    }

    @Specialization
    public final long doSObject(final DynamicObject receiver) {
      return receiver.hashCode();
    }
  }

  @GenerateNodeFactory
  @Primitive(klass = "Object", selector = "objectSize", eagerSpecializable = false)
  @ImportStatic(SObject.class)
  public abstract static class ObjectSizePrim extends UnaryExpressionNode {
    public ObjectSizePrim(final boolean eagWrap, final SourceSection source) {
      super(eagWrap, source);
    }

    @Specialization
    public final long doArray(final Object[] receiver) {
      int size = 0;
      size += receiver.length;
      return size;
    }

    @Specialization // (guards = "isSObject(receiver)")
    public final long doSObject(final DynamicObject receiver) {
      int size = 0;
      size += SObject.getNumberOfFields(receiver);
      return size;
    }

    @Specialization
    public final long doSAbstractObject(final Object receiver) {
      return 0; // TODO: allow polymorphism?
    }
  }
}

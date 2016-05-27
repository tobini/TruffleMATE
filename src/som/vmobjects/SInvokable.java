/**
 * Copyright (c) 2013 Stefan Marr,   stefan.marr@vub.ac.be
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package som.vmobjects;

import som.interpreter.Invokable;
import som.interpreter.SArguments;
import som.vm.Universe;
import som.vm.constants.Classes;
import som.vm.constants.ExecutionLevel;
import som.vm.constants.Nil;
import som.vmobjects.SReflectiveObject.SReflectiveObjectObjectType;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.api.object.dsl.Snippets.BaseLayout;

public class SInvokable {
  
  @Layout
  public interface InvokableLayout {
    SSymbol getSignature(DynamicObject object);
    Invokable getInvokable(DynamicObject object);
    RootCallTarget getCallTarget(DynamicObject object);
    DynamicObject getHolder(DynamicObject object);
    Invokable getInvokableMeta(final DynamicObject object);
    RootCallTarget getCallTargetMeta(final DynamicObject object);
    void setHolderUnsafe(DynamicObject object, DynamicObject value);
    DynamicObject createInvokable(DynamicObjectFactory factory, SSymbol signature, Invokable invokable, RootCallTarget callTarget, Invokable invokableMeta, RootCallTarget callTargetMeta, DynamicObject holder);
    DynamicObjectFactory createInvokableShape(DynamicObject clazz);
  }
    
  private static final MethodObjectType INVOKABLE_TYPE = new MethodObjectType(Nil.nilObject);
  
  private static final DynamicObjectFactory INVOKABLES_FACTORY = InvokableLayoutImpl.INSTANCE.createInvokableShape(Classes.methodClass);
  
  public static DynamicObject create(final SSymbol signature, final Invokable invokable) {
    Invokable invokableMeta = (Invokable) invokable.deepCopy();
    return INVOKABLES_FACTORY.newInstance(
        signature, invokable, invokable.createCallTarget(), invokableMeta, invokableMeta.createCallTarget(), Nil.nilObject);
  }
  
  public static final RootCallTarget getCallTarget(final DynamicObject invokable, final ExecutionLevel level) {
    if (level == ExecutionLevel.Meta){
      return InvokableLayoutImpl.INSTANCE.getCallTargetMeta(invokable);
    }
    return InvokableLayoutImpl.INSTANCE.getCallTarget(invokable); 
  }
  
  public static final int getNumberOfArguments(final DynamicObject invokable) {
    return InvokableLayoutImpl.INSTANCE.getSignature(invokable).getNumberOfSignatureArguments();
  }

  public static final Object invoke(final DynamicObject invokable, final VirtualFrame frame, final Object... arguments) {
    return getCallTarget(invokable,SArguments.getExecutionLevel(frame)).call(arguments);
  }

  public static final Object invoke(final DynamicObject invokable, final VirtualFrame frame, final IndirectCallNode node, final Object... arguments) {
    return node.call(frame, getCallTarget(invokable,SArguments.getExecutionLevel(frame)), arguments);
  }
  
  public static final Object invoke(final DynamicObject invokable, final DynamicObject environment, final ExecutionLevel exLevel, final Object... arguments) {
    return getCallTarget(invokable,exLevel).call(SArguments.createSArguments(environment, exLevel, arguments));
  }
  
  public static final String toString(final DynamicObject invokable) {
    // TODO: fixme: remove special case if possible, I think it indicates a bug
    if (InvokableLayoutImpl.INSTANCE.getHolder(invokable) == null) {
      return "Method(nil>>" + InvokableLayoutImpl.INSTANCE.getSignature(invokable).toString() + ")";
    }

    return "Method(" + SClass.getName(InvokableLayoutImpl.INSTANCE.getHolder(invokable)).getString() + ">>" + 
      InvokableLayoutImpl.INSTANCE.getSignature(invokable).toString() + ")";
  }
  
  public static final class SMethod extends SInvokable {
    @Layout
    public interface SMethodLayout extends InvokableLayout {
      DynamicObject[] getEmbeddedBlocks(final DynamicObject object);
      DynamicObject createSMethod(DynamicObjectFactory factory, SSymbol signature, Invokable invokable, 
          RootCallTarget callTarget, Invokable invokableMeta, RootCallTarget callTargetMeta, DynamicObject holder, DynamicObject[] embeddedBlocks);
    }

    
    private static final MethodObjectType SMETHOD_TYPE           = new MethodObjectType(Nil.nilObject);
    private static final DynamicObjectFactory SMETHOD_FACTORY    = InvokableLayoutImpl.INSTANCE.createInvokableShape(Classes.methodClass);
    
    public static DynamicObject create(final SSymbol signature, final Invokable invokable, final DynamicObject[] embeddedBlocks) {
      Invokable invokableMeta = (Invokable) invokable.deepCopy();
      return SMETHOD_FACTORY.newInstance(
          signature, invokable, invokable.createCallTarget(), invokableMeta, invokableMeta.createCallTarget(), Nil.nilObject, embeddedBlocks);
    }
    
    public static void setHolder(final DynamicObject invokable, final DynamicObject value) {
      SMethodLayoutImpl.INSTANCE.setHolderUnsafe(invokable, value);
      for (DynamicObject methods : SMethodLayoutImpl.INSTANCE.getEmbeddedBlocks(invokable)) {
        SMethodLayoutImpl.INSTANCE.setHolderUnsafe(methods, value);
      }
    }
    
    public static boolean isSMethod(final DynamicObject obj) {
      return SInvokableLayoutImpl.INSTANCE.getObjectType() == SMethodType;
    }
  }
  
  public static final class SPrimitive extends SInvokable {
    private static final Shape PRIMITIVES_SHAPE = createInvokablesShape(Classes.methodClass);
    private static final DynamicObjectFactory PRIMITIVES_FACTORY = PRIMITIVES_SHAPE.createFactory();
    
    public static DynamicObject create(final SSymbol signature, final Invokable invokable) {
      Invokable invokableMeta = (Invokable) invokable.deepCopy();
      return PRIMITIVES_FACTORY.newInstance(
          signature, invokable, invokable.createCallTarget(), invokableMeta, invokableMeta.createCallTarget(), Nil.nilObject);
    }
    
    public static boolean isSPrimitive(final DynamicObject obj) {
      return obj.getShape().getObjectType() == INVOKABLE_TYPE;
    }
  }
}
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

public class SInvokable {
  private static final MethodObjectType INVOKABLE_TYPE = new MethodObjectType(Nil.nilObject);
  
  private static final SSymbol SIGNATURE      = Universe.current().symbolFor("signature");
  private static final SSymbol INVOKABLE      = Universe.current().symbolFor("invokable");
  private static final SSymbol CALLTARGET     = Universe.current().symbolFor("callTarget");
  private static final SSymbol INVOKABLEMETA  = Universe.current().symbolFor("invokableMeta");
  private static final SSymbol CALLTARGETMETA = Universe.current().symbolFor("callTargetMeta");
  private static final SSymbol HOLDER         = Universe.current().symbolFor("holder");
  
  private static final Shape INVOKABLES_SHAPE = createInvokablesShape(Classes.methodClass);
  private static final DynamicObjectFactory INVOKABLES_FACTORY = INVOKABLES_SHAPE.createFactory();
  
  private static Shape createInvokablesShape(final DynamicObject clazz) {
    return SReflectiveObject.LAYOUT.createShape(INVOKABLE_TYPE, clazz).
        defineProperty(SIGNATURE,      Nil.nilObject, 0).
        defineProperty(INVOKABLE,      Nil.nilObject, 0).
        defineProperty(CALLTARGET,     Nil.nilObject, 0).
        defineProperty(INVOKABLEMETA,  Nil.nilObject, 0).
        defineProperty(CALLTARGETMETA, Nil.nilObject, 0).
        defineProperty(HOLDER,         Nil.nilObject, 0);
  }
  
  private static final class MethodObjectType extends SReflectiveObjectObjectType {
    public MethodObjectType(DynamicObject metaobj) {
      super(metaobj);
    }

    @Override
    public String toString() {
      return "SInvokable";
    }
  }
  
  public static DynamicObject create(final SSymbol signature, final Invokable invokable) {
    Invokable invokableMeta = (Invokable) invokable.deepCopy();
    return INVOKABLES_FACTORY.newInstance(
        signature, invokable, invokable.createCallTarget(), invokableMeta, invokableMeta.createCallTarget(), Nil.nilObject);
  }
  
  public static final RootCallTarget getCallTarget(final DynamicObject invokable, final ExecutionLevel level) {
    if (level == ExecutionLevel.Meta){
      return getCallTargetMeta(invokable);
    }
    return getCallTarget(invokable); 
  }
  
  public static final RootCallTarget getCallTarget(final DynamicObject invokable) {
    return (RootCallTarget) invokable.get(CALLTARGET);
  }
  
  public static final RootCallTarget getCallTargetMeta(final DynamicObject invokable) {
    return (RootCallTarget) invokable.get(CALLTARGETMETA);
  }

  public static final Invokable getInvokable(final DynamicObject invokable) {
    return (Invokable) invokable.get(INVOKABLE);
  }

  public static final SSymbol getSignature(final DynamicObject invokable) {
    return (SSymbol) invokable.get(SIGNATURE);
  }
  
  public static final DynamicObject getHolder(final DynamicObject invokable) {
    return (DynamicObject) invokable.get(HOLDER);
  }

  public static void setHolder(final DynamicObject invokable, final DynamicObject value) {
    if (SMethod.isSMethod(value)){
      SMethod.setHolder(invokable, value);
    } else {
      invokable.set(HOLDER, value);
    }
  }

  
  public static final int getNumberOfArguments(final DynamicObject invokable) {
    return getSignature(invokable).getNumberOfSignatureArguments();
  }

  public static final Object invoke(final DynamicObject invokable, final Object... arguments) {
    return getCallTarget(invokable).call(arguments);
  }

  public static final Object invoke(final DynamicObject invokable, final VirtualFrame frame, final IndirectCallNode node, final Object... arguments) {
    return node.call(frame, getCallTarget(invokable), arguments);
  }
  
  public static final Object invoke(final DynamicObject invokable, final DynamicObject environment, final ExecutionLevel exLevel, final Object... arguments) {
      return getCallTarget(invokable).call(SArguments.createSArguments(environment, exLevel, arguments));
  }
  
  public static final String toString(final DynamicObject invokable) {
    // TODO: fixme: remove special case if possible, I think it indicates a bug
    if (getHolder(invokable) == null) {
      return "Method(nil>>" + getSignature(invokable).toString() + ")";
    }

    return "Method(" + SClass.getName(getHolder(invokable)).getString() + ">>" + getSignature(invokable).toString() + ")";
  }
  
  public static final class SMethod extends SInvokable {
    private static final MethodObjectType SMETHOD_TYPE           = new MethodObjectType(Nil.nilObject);
    private static final SSymbol EMBEDDEDBLOCKS                  = Universe.current().symbolFor("embeddedBlocks");
    private static final Shape SMETHOD_SHAPE                     = createSMethodShape(Classes.methodClass);
    private static final DynamicObjectFactory SMETHOD_FACTORY    = SMETHOD_SHAPE.createFactory();
    
    
    private static Shape createSMethodShape(final DynamicObject clazz) {
      return INVOKABLES_SHAPE.defineProperty(EMBEDDEDBLOCKS, Nil.nilObject, 0).changeType(SMETHOD_TYPE);
    }
    
    public static DynamicObject create(final SSymbol signature, final Invokable invokable, final DynamicObject[] embeddedBlocks) {
      Invokable invokableMeta = (Invokable) invokable.deepCopy();
      return SMETHOD_FACTORY.newInstance(
          signature, invokable, invokable.createCallTarget(), invokableMeta, invokableMeta.createCallTarget(), Nil.nilObject, embeddedBlocks);
    }
    
    public static DynamicObject[] getEmbeddedBlocks(final DynamicObject invokable) {
      return (DynamicObject[])invokable.get(EMBEDDEDBLOCKS);
    }
    
    public static void setHolder(final DynamicObject invokable, final DynamicObject value) {
      SInvokable.setHolder(invokable, value);
      for (DynamicObject methods : getEmbeddedBlocks(invokable)) {
        setHolder(methods, value);
      }
    }
    
    public static boolean isSMethod(final DynamicObject obj) {
      return obj.getShape().getObjectType() == SMETHOD_TYPE;
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
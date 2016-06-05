/**
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

import som.vm.NotYetImplementedException;
import som.vm.Universe;
import som.vm.constants.Nil;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.dsl.Layout;


public class SObject {
  
  @Layout
  public interface SObjectLayout {
    DynamicObject createSObject(DynamicObjectFactory factory);
    DynamicObjectFactory createSObjectShape(DynamicObject klass);
    DynamicObject getKlass(DynamicObjectFactory factory);
    DynamicObject getKlass(ObjectType objectType);
    DynamicObject getKlass(DynamicObject object);
    void setKlass(DynamicObject object, DynamicObject value);
    boolean isSObject(DynamicObject object);
    boolean isSObject(ObjectType objectType);
  }
  
  public static final DynamicObjectFactory NIL_DUMMY_FACTORY = SObjectLayoutImpl.INSTANCE.createSObjectShape(Nil.nilObject);
  
  public static DynamicObjectFactory createObjectShapeFactoryForClass(final DynamicObject clazz) {
    return SObjectLayoutImpl.INSTANCE.createSObjectShape(clazz);
  }

  public static DynamicObject create(final DynamicObject instanceClass) {
    CompilerAsserts.neverPartOfCompilation("Basic create without factory caching");
    DynamicObjectFactory factory = SClass.getFactory(instanceClass);
    //The parameter is the metaobject, only valid for SReflectiveObjects
    return factory.newInstance(Nil.nilObject);
  }
  
  public static DynamicObject createNil() {
    // TODO: this is work in progress, the class should go as shared data into the shape
    // TODO: ideally, nil is like in SOMns an SObjectWithoutFields
    return SObjectLayoutImpl.INSTANCE.createSObjectShape(null).newInstance();
    //NIL_DUMMY_FACTORY.newInstance(new Object[] { null });
  }
  
  /**
   * For SObjects, we store the class in the shape's shared data.
   * This makes sure that each class has a separate shape tree and the shapes
   * can be used for field accesses as well as message sends as guards.
   * Without the separation, it could well be that objects from two different
   * classes end up with the same shape, which would mean shapes could not be
   * used as guards for message sends, because it would not be guaranteed that
   * the right message is send/method is activated.
   *
   * Note, the SClasses store their class as a property, to avoid having
   * multiple shapes for each basic classes.
   */
  public static boolean isSObject(final DynamicObject obj) {
    return SObjectLayoutImpl.INSTANCE.isSObject(obj);
  }

  /*private static final class SObjectObjectType extends ObjectType {
    @Override
    public String toString() {
      return "SObject";
    }
  }*/

  public static DynamicObject getSOMClass(final DynamicObject obj) {
    //Todo: Remove the if and make this method homogeneous when all objects use the @layout annotation
    return SObjectLayoutImpl.INSTANCE.getKlass(obj);
    /*DynamicObject type = (DynamicObject) obj.getShape().getSharedData();
    if (type == null) type = SInvokable.getSOMClass(obj);
    return type;*/
  }

  public static final void internalSetNilClass(final DynamicObject object, final DynamicObject value) {
    assert object != null;
    assert value != null;
    assert !Universe.current().objectSystemInitialized : "This should really only be used during initialization of object system";
    SObjectLayoutImpl.INSTANCE.setKlass(object, value);
  }
  
  public static final int getNumberOfFields(final DynamicObject obj) {
    throw new NotYetImplementedException();
  }
}


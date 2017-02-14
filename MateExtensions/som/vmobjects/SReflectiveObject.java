/**
 * Copyright (c) 2015 Guido Chari, gchari@dc.uba.ar
 * LaFHIS lab, Universidad de Buenos Aires, Buenos Aires, Argentina
 * http://www.lafhis.dc.uba.ar
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

import som.vm.Universe;
import som.vm.constants.Nil;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.object.dsl.Layout;

public class SReflectiveObject extends SObject {
  @Layout
  public interface SReflectiveObjectLayout extends SObjectLayout {
    // DynamicObject createSReflectiveObject(DynamicObjectFactory factory);
    // DynamicObject getEnvironment(DynamicObjectFactory factory);
    DynamicObjectFactory createSReflectiveObjectShape(DynamicObject klass, DynamicObject environment);
    DynamicObject getEnvironment(ObjectType objectType);
    DynamicObject getEnvironment(DynamicObject object);
    void setEnvironment(DynamicObject object, DynamicObject value);
    boolean isSReflectiveObject(DynamicObject object);
    boolean isSReflectiveObject(ObjectType objectType);
    Object[] build();
  }

  // Only needed for system initialization
  public static final DynamicObjectFactory SREFLECTIVE_OBJECT_FACTORY =
      SReflectiveObjectLayoutImpl.INSTANCE.createSReflectiveObjectShape(Nil.nilObject, Nil.nilObject);

  public static final DynamicObject getEnvironment(final DynamicObject obj) {
    return SReflectiveObjectLayoutImpl.INSTANCE.getEnvironment(obj);
  }

  public static final DynamicObject getEnvironment(final Shape shape) {
    return SReflectiveObjectLayoutImpl.INSTANCE.getEnvironment(shape.getObjectType());
  }

  @Override
  public final Object[] buildArguments() {
    return SReflectiveObjectLayoutImpl.INSTANCE.build();
  }

  public static final void setEnvironment(final DynamicObject obj, final DynamicObject value) {
    ObjectType cachedType = Universe.getCurrent().getCachedObjectType(SObject.getSOMClass(obj), value);
    if (cachedType != null) {
      obj.getShape().changeType(cachedType);
    } else {
      SReflectiveObjectLayoutImpl.INSTANCE.setEnvironment(obj, value);
      Universe.getCurrent().cacheNewObjectType(SObject.getSOMClass(obj), obj.getShape().getObjectType());
    }
  }

  public static boolean isSReflectiveObject(final DynamicObject obj) {
    return SReflectiveObjectLayoutImpl.INSTANCE.isSReflectiveObject(obj);
  }

  public static boolean isSReflectiveObject(ObjectType type) {
    return SReflectiveObjectLayoutImpl.INSTANCE.isSReflectiveObject(type);
  }

  public static DynamicObjectFactory createObjectShapeFactoryForClass(final DynamicObject clazz) {
    return SReflectiveObjectLayoutImpl.INSTANCE.createSReflectiveObjectShape(clazz, Nil.nilObject);
  }
}

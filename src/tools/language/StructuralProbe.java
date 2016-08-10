package tools.language;

import java.util.HashSet;
import java.util.Set;

import com.oracle.truffle.api.object.DynamicObject;


public class StructuralProbe {

  protected final Set<DynamicObject> classes;
  protected final Set<DynamicObject> methods;

  public StructuralProbe() {
    classes = new HashSet<>();
    methods = new HashSet<>();
  }

  public void recordNewClass(final DynamicObject clazz) {
    classes.add(clazz);
  }

  public void recordNewMethod(final DynamicObject method) {
    methods.add(method);
  }

  public Set<DynamicObject> getClasses() {
    return classes;
  }

  public Set<DynamicObject> getMethods() {
    return methods;
  }
}

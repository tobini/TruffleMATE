package som.vm.constants;

import som.vm.Universe;
import com.oracle.truffle.api.object.DynamicObject;


public final class Nil {
  public static final DynamicObject nilObject;

  static {
    nilObject = Universe.current().createNilObject();
  }
}

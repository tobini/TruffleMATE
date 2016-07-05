package som.vm.constants;

import som.vm.Universe;

import com.oracle.truffle.api.object.DynamicObject;



public final class Globals {
  public static final DynamicObject trueObject;
  public static final DynamicObject falseObject;
  public static final DynamicObject systemObject;

 static {
    trueObject   = Universe.current().getTrueObject();
    falseObject  = Universe.current().getFalseObject();
    systemObject = Universe.current().getSystemObject();
  }
}

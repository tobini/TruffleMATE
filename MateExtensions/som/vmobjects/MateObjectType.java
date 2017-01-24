package som.vmobjects;

import som.interop.DynamicObjectInteropMessageResolutionForeign;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;


public class MateObjectType extends ObjectType {
  @Override
  public ForeignAccess getForeignAccessFactory(DynamicObject object) {
    return DynamicObjectInteropMessageResolutionForeign.createAccess();
}
}

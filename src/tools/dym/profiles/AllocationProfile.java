package tools.dym.profiles;

import som.vmobjects.SClass;
import som.vmobjects.SObject;
import tools.dym.profiles.AllocationProfileFactory.AllocProfileNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.source.SourceSection;


public class AllocationProfile extends Counter {

  protected final AllocProfileNode profile;

  public AllocationProfile(final SourceSection source) {
    super(source);
    this.profile = AllocProfileNodeGen.create();
  }

  public AllocProfileNode getProfile() {
    return profile;
  }

  public int getNumberOfObjectFields() {
    return profile.getNumberOfFields();
  }

  public String getTypeName() {
    return profile.getTypeName();
  }

  public abstract static class AllocProfileNode extends Node {
    protected int numFields = -1;
    protected DynamicObjectFactory classFactory;

    public abstract void executeProfiling(Object obj);

    public int getNumberOfFields() {
      return numFields;
    }

    public String getTypeName() {
      return classFactory.getShape().getObjectType().toString();
    }

    protected DynamicObjectFactory create(final DynamicObjectFactory factory) {
      int n;
      if (factory.getShape() == null) {
        n = 0;
      } else {
        n = factory.getShape().getPropertyCount();
      }
      if (numFields == -1) {
        numFields = n;
        classFactory = factory;
      } else {
        assert numFields == n;
      }
      return factory;
    }

    @Specialization(guards = "getFactory(obj) == factory", limit = "1")
    public void doDynamicObject(final DynamicObject obj,
        @Cached("create(getFactory(obj))") final DynamicObjectFactory factory) { }

    @SuppressWarnings("unused")
    protected static DynamicObjectFactory getFactory(final DynamicObject object) {
      return SClass.getFactory(SObject.getSOMClass(object));
    }
  }
}

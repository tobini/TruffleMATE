package tools.dym.nodes;

import java.math.BigInteger;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

import som.interpreter.Types;
import som.vmobjects.SArray;
import som.vmobjects.SBlock;
import som.vmobjects.SSymbol;
import tools.dym.profiles.CreateCounter;
import tools.dym.profiles.ReadValueProfile.ProfileCounter;


public abstract class TypeProfileNode extends Node {
  protected final CreateCounter profile;

  protected TypeProfileNode(final CreateCounter profile) {
    this.profile = profile;
  }

  public abstract void executeProfiling(Object obj);

  protected ProfileCounter create(final Object obj) {
      return profile.createCounter(Types.getClassOf(obj).getShape());
  }

  @Specialization
  public void doLong(final long obj,
      @Cached("create(obj)") final ProfileCounter cnt) {
    cnt.inc();
  }

  @Specialization
  public void doBigInt(final BigInteger obj,
      @Cached("create(obj)") final ProfileCounter cnt) {
    cnt.inc();
  }

  @Specialization
  public void doDouble(final double obj,
      @Cached("create(obj)") final ProfileCounter cnt) {
    cnt.inc();
  }

  @Specialization
  public void doString(final String obj,
      @Cached("create(obj)") final ProfileCounter cnt) {
    cnt.inc();
  }

  @Specialization(guards = "obj")
  public void doTrue(final boolean obj,
      @Cached("create(obj)") final ProfileCounter cnt) {
    cnt.inc();
  }

  @Specialization(guards = "!obj")
  public void doFalse(final boolean obj,
      @Cached("create(obj)") final ProfileCounter cnt) {
    cnt.inc();
  }

  @Specialization
  public void doSSymbol(final SSymbol obj,
      @Cached("create(obj)") final ProfileCounter cnt) {
    cnt.inc();
  }

  // TODO: we should support different block classes here, but, well, this is
  //       not really interesting for our metrics at the moment
  @Specialization
  public void doSBlock(final SBlock obj,
      @Cached("create(obj)") final ProfileCounter cnt) {
    cnt.inc();
  }

  @Specialization
  public void doSArray(final SArray obj,
      @Cached("create(obj)") final ProfileCounter cnt) {
    cnt.inc();
  }

  @Specialization(guards = "obj.getShape() == shape", limit = "100")
  public void doDynamicObject(final DynamicObject obj,
      @Cached("obj.getShape()") final Shape shape,
      @Cached("profile.createCounter(shape)") final ProfileCounter cnt) {
    cnt.inc();
  }
}

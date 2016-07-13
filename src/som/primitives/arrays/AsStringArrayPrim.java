package som.primitives.arrays;

import java.nio.charset.Charset;

import som.interpreter.nodes.nary.UnaryExpressionNode;
import som.vmobjects.SArray;
import som.vmobjects.SArray.ArrayType;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ValueProfile;


@GenerateNodeFactory
@ImportStatic(ArrayType.class)
public abstract class AsStringArrayPrim extends UnaryExpressionNode {

  private final ValueProfile storageType = ValueProfile.createClassProfile();

  @Specialization(guards = {"isEmptyType(receiver)"})
  public final String doEmptySArray(final SArray receiver) {
    return "";
  }

  @Specialization(guards = "isObjectType(receiver)")
  public final Object doObjectSArray(final SArray receiver) {
    return arrayToString(receiver.getObjectStorage(storageType));
  }
  
  /*Todo: Rewrite this to avoid all this code duplication because of typing*/
  @Specialization(guards = "isLongType(receiver)")
  public final Object doLongSArray(final SArray receiver) {
    long[] numbers = receiver.getLongStorage(storageType);
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < numbers.length; i++){
      output = output.append(numbers[i]);
    }
    return output.toString();
  }
  
  @Specialization(guards = "isPartiallyEmptyType(receiver)")
  public final Object doPartiallyEmptyObjectSArray(final SArray receiver) {
    return arrayToString(receiver.getPartiallyEmptyStorage(storageType).getStorage());
  }
  
  @Specialization(guards = "isByteType(receiver)")
  public final String doByteSArray(final SArray receiver) {
   byte[] bytes = receiver.getByteStorage(storageType);
   return new String(bytes, 0 , bytes.length, Charset.forName("UTF-8"));
  }
  
  private String arrayToString(Object[] chars){
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < chars.length; i++){
      output = output.append(chars[i]);
    }
    return output.toString();
  }
}

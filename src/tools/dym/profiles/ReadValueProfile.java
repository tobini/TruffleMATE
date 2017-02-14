package tools.dym.profiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import som.vm.Universe;

import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.SourceSection;


public class ReadValueProfile extends Counter implements CreateCounter {

  private final Map<Shape, Integer> typesOfReadValue;
  private final List<ProfileCounter> counters;

  // TODO: add support for reading fields from profiled type of receiver objects.
  // need subexpression support for that

  public ReadValueProfile(final SourceSection source) {
    super(source);
    typesOfReadValue = new HashMap<>();
    counters = new ArrayList<>();
  }

  public void profileValueType(final Shape valueType) {
    Universe.callerNeedsToBeOptimized("This is a fallback method");
    typesOfReadValue.merge(valueType, 1, Integer::sum);
  }

  public Map<Shape, Integer> getTypeProfile() {
    Map<Shape, Integer> result = new HashMap<>(typesOfReadValue);
    for (ProfileCounter c : counters) {
      Integer val = result.get(c.getType());
      if (val == null) {
        result.put(c.getType(), c.getValue());
      } else {
        result.put(c.getType(), c.getValue() + val);
      }
    }
    return result;
  }

  @Override
  public ProfileCounter createCounter(final Shape type) {
    ProfileCounter counter = new ProfileCounter(type);
    counters.add(counter);
    return counter;
  }

  public static final class ProfileCounter {
    private int count;
    private final Shape type;

    public ProfileCounter(final Shape type) {
      this.type = type;
    }

    public void inc() {
      count += 1;
    }

    public Shape getType() {
      return type;
    }

    public int getValue() {
      return count;
    }
  }
}

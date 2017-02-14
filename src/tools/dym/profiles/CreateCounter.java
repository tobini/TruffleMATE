package tools.dym.profiles;

import com.oracle.truffle.api.object.Shape;

import tools.dym.profiles.ReadValueProfile.ProfileCounter;


public interface CreateCounter {
  ProfileCounter createCounter(Shape factory);
}

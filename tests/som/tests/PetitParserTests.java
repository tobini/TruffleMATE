package som.tests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import som.vm.Universe;

@RunWith(Parameterized.class)
public class PetitParserTests {

  @Parameters
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"PPArithmeticParserTest"  },
        {"PPConditionalParserTest" },
        {"PPContextMementoTest"    },
        {"PPContextTest"           },
        {"PPExpressionParserTest"  },
        {"PPExtensionTest"         },
        {"PPLambdaParserTest"      },
        {"PPMappingTest"           },
        {"PPObjectTest"            },
        {"PPParserTest"            },
        {"PPPredicateTest"         },
        {"PPScriptingTest"         },
        {"PPTokenTest"             }
      });
  }

  protected String testName;

  public PetitParserTests(final String testName) {
    this.testName = testName;
  }

  @Test
  public void testSomeTest() {
    u.setAvoidExit(true);
    String[] args = this.getArguments();

    u.interpret(args);

    assertEquals(0, u.lastExitCode());
  }
  
  protected String[] getArguments(){
    String[] arg = {"-cp", "Smalltalk:Smalltalk/PetitParser:Smalltalk/Collections/Streams:TestSuite/PetitParser", "TestSuite/TestHarness.som", testName};
    return arg;
  }

  protected static Universe u = Universe.current();
}

package som.tests;

import java.util.Arrays;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PetitParserTests extends SomTests {

  public PetitParserTests(String testName) {
    super(testName);
  }

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

  protected String[] getArguments(){
    String[] arg = {
        "--mate",
        "-cp", 
        "Smalltalk:Smalltalk/Mate:Smalltalk/Mate/MOP:Smalltalk/PetitParser:Smalltalk/Collections/Streams:TestSuite:TestSuite/PetitParser", 
        "TestHarness", 
        testName};
    return arg;
  }
}

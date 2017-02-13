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
//        {"PPSmalltalkClassesTest"  },
//        {"PPSmalltalkGrammarTest"  },
 //       {"PPSmalltalkParserTest"   }
      });
  }

  protected String[] getArguments() {
    String[] arg = {
        "-cp",
        "Smalltalk:Smalltalk/Mate:Smalltalk/Mate/MOP:Smalltalk/Collections/Streams:"
        + "Smalltalk/PetitParser:Smalltalk/PetitParser/PetitSmalltalk:Smalltalk/AST-Core:Smalltalk/AST-Core/Parser:"
        + "TestSuite:TestSuite/PetitParser:TestSuite/PetitParser/PetitSmalltalk",
        "TestHarness",
        testName};
    return arg;
  }
}

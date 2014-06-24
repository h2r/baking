package edu.brown.cs.h2r.baking.Testing;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class BakingTestRunner {
   public static void main(String[] args) {
      Result result = JUnitCore.runClasses(BakingTestSuite.class);
      for (Failure failure : result.getFailures()) {
         System.out.println(failure.toString());
      }
      System.out.println(result.wasSuccessful());
   }
}
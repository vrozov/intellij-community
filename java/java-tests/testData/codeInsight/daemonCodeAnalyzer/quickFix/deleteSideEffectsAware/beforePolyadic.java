// "Convert to 'if' statement" "true"
import java.io.File;

public class Main {
  public void test(File f) {
    f.isDirectory() |<caret>| f.mkdirs();
  }
}
package gitlet;

import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Duc Nguyen
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }

    @Test
    public void testCommitBasic() {
        HashMap<String, String> filesToBlobs = new HashMap<>();
        String firstParentHash = "first parent reference using SHA-1";
        firstParentHash = Utils.sha1(firstParentHash);
        Commit tmp = new Commit("initial commit", filesToBlobs,
                firstParentHash, "", "master");
        assertEquals("initial commit", tmp.getMsg());
        assertEquals(firstParentHash, tmp.getParentHash());
    }
}



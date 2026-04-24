import org.junit.Test;
import java.io.*;

public class CanaryTest {
    @Test
    public void testCanary() throws Exception {
        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "echo 'CANARY-HB-X7K2M9'"});
        p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }
}

package strike;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StrikeServerTest {

    private StrikeCommon sc;

    @Before
    public void before() {
        sc = new StrikeCommon();
        System.out.println("Before Test");
    }

    @After
    public void after() {
        System.out.println("After Test");
    }

    @Test
    public void testHello() {
        assertEquals(sc.hello(), "Hello");
    }

    @Test @Ignore
    public void testIgnore() {
        System.out.println(sc.hello());
    }
}

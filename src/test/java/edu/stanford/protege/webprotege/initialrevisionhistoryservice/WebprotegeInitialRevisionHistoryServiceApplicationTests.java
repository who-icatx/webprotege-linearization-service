package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith({SpringExtension.class, IntegrationTest.class})
public class WebprotegeInitialRevisionHistoryServiceApplicationTests {

    @Test
    public void contextLoads() {
    }

}

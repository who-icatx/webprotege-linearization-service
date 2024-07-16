package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history.EntityLinearizationHistoryRepository;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest
@Import({WebprotegeLinearizationServiceServiceApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ExtendWith(IntegrationTest.class)
@RunWith(SpringRunner.class)
class LinearizationHistoryServiceTest {

    @Autowired
    EntityLinearizationHistoryRepository linearizationHistoryRepository;

    @Mock
    LinearizationEventMapper eventMapper;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    RedissonService redissonService;

    @InjectMocks
    LinearizationHistoryService linearizationHistoryService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        linearizationHistoryService = new LinearizationHistoryService(objectMapper, linearizationHistoryRepository, eventMapper, redissonService);
    }

    @Test
    void GIVEN_noPreviousHistoryForEntity_WHEN_addingNewRevision_THEN_newHistoryIsCreatedWithSaidRevision() {
//        var whoficSpec =
//        linearizationHistoryService.addRevision();
    }
}
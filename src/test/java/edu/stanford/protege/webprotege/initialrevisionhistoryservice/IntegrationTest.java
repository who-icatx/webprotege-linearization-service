package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2023-06-06
 */
public abstract class IntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(IntegrationTest.class);

    private static MongoDBContainer mongoDBContainer;

    @BeforeClass
    public static void setUpContainers(){
        setUpMongo();
    }

    private static void setUpMongo(){
        var imageName = DockerImageName.parse("mongo");
        mongoDBContainer = new MongoDBContainer(imageName)
                .withExposedPorts(27017, 27017);
        mongoDBContainer.start();

        var mappedHttpPort = mongoDBContainer.getMappedPort(27017);
        logger.info("MongoDB port 27017 is mapped to {}", mappedHttpPort);
        System.setProperty("spring.data.mongodb.port", Integer.toString(mappedHttpPort));
    }

    @AfterClass
    public static void closeContainers(){
        mongoDBContainer.close();
    }

}

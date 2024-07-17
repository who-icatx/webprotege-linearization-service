package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.extension.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.*;
import org.testcontainers.utility.DockerImageName;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2023-06-06
 */
public abstract class IntegrationTest {

    private final static Logger logger = LoggerFactory.getLogger(IntegrationTest.class);

    private static MongoDBContainer mongoDBContainer;
    private static RabbitMQContainer rabbitContainer;

    @BeforeClass
    public static void setUpContainers(){
        setUpMongo();
        setUpRabbitMq();
    }

    private static void setUpMongo(){
        var imageName = DockerImageName.parse("mongo");
        mongoDBContainer = new MongoDBContainer(imageName)
                .withExposedPorts(27017);
        mongoDBContainer.start();

        var mappedHttpPort = mongoDBContainer.getMappedPort(27017);
        logger.info("MongoDB port 27017 is mapped to {}", mappedHttpPort);
        System.setProperty("spring.data.mongodb.port", Integer.toString(mappedHttpPort));
    }

    private static void setUpRabbitMq(){
        var imageName = DockerImageName.parse("rabbitmq:3.7.25-management-alpine");
        rabbitContainer = new RabbitMQContainer(imageName)
                .withExposedPorts(5672);
        rabbitContainer.start();

        System.setProperty("spring.rabbitmq.host", rabbitContainer.getHost());
        System.setProperty("spring.rabbitmq.port", String.valueOf(rabbitContainer.getAmqpPort()));
    }

    @AfterClass
    public static void closeContainers(){
        mongoDBContainer.close();
        rabbitContainer.close();
    }

}

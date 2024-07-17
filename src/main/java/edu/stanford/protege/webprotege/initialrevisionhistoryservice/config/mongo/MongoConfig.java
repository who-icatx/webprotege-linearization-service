package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.mongo.codecs.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.mongo.converters.*;
import org.bson.codecs.configuration.*;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Arrays;
import java.util.List;

@Configuration
public class MongoConfig {

    @Autowired
    private MongoProperties mongoProperties;

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = Arrays.asList(
                new IriReadConverter(), new IriWriteConverter(),
                new ProjectIdReadConverter(), new ProjectIdWriteConverter(),
                new ThreeStateBooleanReadConverter(), new ThreeStateBooleanWriteConverter()
        );
        return new MongoCustomConversions(converters);
    }

    @Bean
    public MongoClient customMongoClient() {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(new IriCodec(), new ProjectIdCodec(), new ThreeStateBooleanCodec()),
                CodecRegistries.fromProviders(pojoCodecProvider),
                MongoClientSettings.getDefaultCodecRegistry()
        );

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoProperties.getUri()))
                .codecRegistry(codecRegistry)
                .build();

        return MongoClients.create(clientSettings);
    }

    @Bean
    public MongoDatabaseFactory customMongoDbFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, mongoProperties.getDatabase());
    }

    @Bean
    public MappingMongoConverter customMappingMongoConverter(MongoDatabaseFactory factory, MongoMappingContext context, MongoCustomConversions conversions) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, context);
        converter.setCustomConversions(conversions);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null)); // Remove _class field
        converter.afterPropertiesSet();
        return converter;
    }

    @Bean
    @Qualifier("enhancedMongoTemplate")
    public MongoTemplate enhancedMongoTemplate(MongoDatabaseFactory factory, MappingMongoConverter converter) {
        return new MongoTemplate(factory, converter);
    }
}









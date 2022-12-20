package io.formhero.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.formhero.params.ServiceParamsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

class MongoConfig {
    private static final Logger log = LogManager.getLogger(MongoConfig.class.getName());
    private final ServiceParamsManager serviceParamsManager;
    private final Document configDocument;


    public MongoConfig(ServiceParamsManager serviceParamsManager, String environmentOwner, String environment) {
        super();
        this.serviceParamsManager = serviceParamsManager;
        this.configDocument = loadConfig(environmentOwner, environment);
    }

    private Document loadConfig(String environmentOwner, String environment) {
        String connectionUrl = getMongoDBConnectionUrl();
        MongoClientURI mongoURI = new MongoClientURI(connectionUrl);
        MongoClient mongoClient = new MongoClient(mongoURI);

        MongoDatabase database = mongoClient.getDatabase(getServiceParamValue("mongodb/configdb", "formhero-config"));
        MongoCollection configCollection = database.getCollection("formhero-configs");
        FindIterable<Document> results = configCollection.find(
                and(
                        eq("environmentOwner", environmentOwner),
                        eq("environment", environment)));
        return results.first();
    }

    private String getMongoDBConnectionUrl() {
        String hostNames = getServiceParamValue("mongodb/host");
        boolean useSeedlist = Boolean.parseBoolean(getServiceParamValue("mongodb/useSeedlist", "false"));

        // This if statement is if we are using DNS Seedlist connection type
        // which is what is used by MongoDB Atlas

        StringBuilder stringBuilder = new StringBuilder();
        if (useSeedlist) {
            stringBuilder.append("mongodb+srv://")
                         .append(getServiceParamValue("mongodb/username"))
                         .append(":")
                         .append(getServiceParamValue("mongodb/password"))
                         .append("@")
                         .append(hostNames)
                         .append("/")
                         .append(getServiceParamValue("mongodb/configdb", "formhero-config"))
                         .append("?retryWrites=true&w=majority");
        } else {
            stringBuilder.append("mongodb://")
                         .append(getServiceParamValue("mongodb/username"))
                         .append(":")
                         .append(getServiceParamValue("mongodb/password"))
                         .append("@")
                         .append(hostNames)
                         .append("/")
                         .append(getServiceParamValue("mongodb/configdb", "formhero-config"))
                         .append("?authSource=")
                         .append(getServiceParamValue("mongodb/authDb", "admin"));
        }

        // Optional mixin replicaSet configuration
        // The driver does not like replicaSet when only one host specified
        if (hostNames != null && hostNames.contains(",")) {
            String replicaSet = getServiceParamValue("mongodb/replicaSet");
            if (replicaSet != null) {
                stringBuilder.append("&replicaSet=")
                             .append(replicaSet);
            }
        }

        log.warn("Connecting to: " + hostNames);
        return stringBuilder.toString();
    }

    private String getServiceParamValue(String paramKey, String defaultValue) {
        return this.serviceParamsManager.getParamFromEnvVarOrParameterStore(paramKey, defaultValue);
    }

    private String getServiceParamValue(String paramKey) {
        return this.serviceParamsManager.getParamFromEnvVarOrParameterStore(paramKey);
    }

    public String toJSON() {
        return com.mongodb.util.JSON.serialize(configDocument.get("config"));
    }
}

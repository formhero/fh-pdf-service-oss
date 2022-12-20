package io.formhero.util;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.formhero.params.Ec2Params;
import io.formhero.params.EnvVarParams;
import io.formhero.params.ServiceParamsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static io.formhero.params.ServiceParamsManager.ParamType.*;

public class FhConfig {

    private static final Logger log = LogManager.getLogger(FhConfig.class.getName());
    private final Config config;
    private final ServiceParamsManager serviceParamsManager;

    public FhConfig(Config config, ServiceParamsManager serviceParamsManager) {
        this.config = config;
        this.serviceParamsManager = serviceParamsManager;
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public Number getNumber(String path) {
        return config.getNumber(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public int getInt(String path, int defaultValue) {
        if (config.hasPath(path)) {
            return config.getInt(path);
        } else {
            return defaultValue;
        }
    }

    public long getLong(String path) {
        return config.getLong(path);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public List<FhConfig> getSubConfigList(String path) {
        try {
            List<? extends Config> configList = config.getConfigList(path);
            List<FhConfig> results = new ArrayList<>();
            for (Config config : configList) {
                results.add(new FhConfig(config, serviceParamsManager));
            }
            return results;
        } catch (Throwable t) {
            t.printStackTrace();
            return new ArrayList<>();
        }
    }


    public String getString(String path) {
        if (config.hasPath(path)) {
            String value = config.getString(path);

            if (value != null && value.startsWith(EC2_PARAM.getParamPrefix()) && value.endsWith(")")) {
                return getStringFromEc2Param(path, value);
            } else if (value != null && value.startsWith(ENV_VAR_PARAM.getParamPrefix()) && value.endsWith(")")) {
                return getStringFromEnvVar(path, value);
            } else {
                return value;
            }
        } else {
            return null;
        }
    }

    private String getStringFromEc2Param(String path, String value) {
        try {
            return getParamValue(value, EC2_PARAM);
        } catch (Throwable t) {
            log.error("Unable to decrypt \"" + path + "\" property when reading from AWS Parameter Store:", t);
            return null;
        }
    }

    private String getStringFromEnvVar(String path, String value) {
        try {
            return getParamValue(value, ENV_VAR_PARAM);
        } catch (Throwable t) {
            log.error("Unable to decrypt \"" + path + "\" property when reading from env-vars:", t);
            return null;
        }
    }

    private String getParamValue(String value, ServiceParamsManager.ParamType envVarParam) {
        String key = value.substring(envVarParam.getParamPrefix().length(), value.length() - 1);
        return serviceParamsManager.get(key, envVarParam);
    }

    public static void main(String[] args) {
        try {
            FhConfig config = loadConfig();
            System.out.println("This is the config.ec2ParamTest: " + config.getString("ec2ParamTest"));
            System.out.println("This is the config.redisClusterExample.cluster.0.host portion: " + config.getSubConfigList(
                    "redisClusterExample.connectOptions.cluster").get(0).getString("host"));
            System.out.println("This is the config.redis.connectOptions.host portion: " + config.getString("redis.connectOptions.host"));
        } catch (Throwable t) {
            System.out.println("Something is not right:");
            t.printStackTrace();
        }
    }

    public static FhConfig loadConfig() throws FhConfigException {
        // Load EC2 parameters
        String environment = System.getenv().get("FORMHERO_ENVIRONMENT");
        String environmentOwner = System.getenv().get("FORMHERO_ENVIRONMENT_OWNER");
        log.info("Loading configuration for " + environment + "/" + environmentOwner + "...");
        ServiceParamsManager serviceParamsManager = ServiceParamsManager.builder()
                                                                        .ec2Params(new Ec2Params(environmentOwner, environment))
                                                                        .envVarParams(new EnvVarParams())
                                                                        .build();

        // Load JSON configuration from database
        MongoConfig dbConfig = new MongoConfig(serviceParamsManager, environmentOwner, environment);
        String jsonConfig = dbConfig.toJSON();

        // If FORMHERO_CONFIG_FILE variable is defined, override values loaded from the database
        String configFilePath = System.getenv().get("FORMHERO_CONFIG_FILE");
        if (configFilePath != null) {
            OverrideConfig overrideConfig = new OverrideConfig();

            try {
                // Load JSON override configuration
                JSONObject jsonOverride = overrideConfig.loadConfig(configFilePath);

                // Merge override configuration into database configuration
                if (jsonOverride != null) {
                    JSONObject mergeConfig = overrideConfig.mergeConfig(jsonOverride, new JSONObject(jsonConfig));
                    if (mergeConfig != null) {
                        jsonConfig = mergeConfig.toString();
                    }
                }
            } catch (Throwable t) {
                log.warn("Unable to load and merge override configuration for " + configFilePath);
            }
        }

        // Substitute EC2 parameters into configuration
        return new FhConfig(ConfigFactory.parseReader(new StringReader(jsonConfig)), serviceParamsManager);
    }
}

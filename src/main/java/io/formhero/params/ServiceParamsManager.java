package io.formhero.params;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ServiceParamsManager {

    private final ServiceParams ec2Params;

    private final ServiceParams envVarParams;

    private String normalizeKey(String key) {
        // DevOps requested keys to be transformed from `mongodb/host` into `MONGODB_HOST`
        if (key.contains("/")) {
            return key.toUpperCase().replace("/", "_");
        }
        return key;
    }

    public String getParamFromEnvVarOrParameterStore(String key) {
        return getParamFromEnvVarOrParameterStore(key, null);
    }

    public String getParamFromEnvVarOrParameterStore(String key, String defaultValue) {
        String value = envVarParams.get(normalizeKey(key));
        if (value != null) {
            return value;
        }
        // If it's not found in the environment, tries to read it from `ec2Params`
        return getEC2Param(key, defaultValue);
    }

    private String getEC2Param(String key, String defaultValue) {
        return ec2Params.get(key, defaultValue);
    }

    private String getEnvVarParam(String key) {
        return envVarParams.get(key, null);
    }


    public String get(String key, ParamType paramType) {
        if (ParamType.EC2_PARAM == paramType) {
            return getEC2Param(key,null);
        }
        return getEnvVarParam(key);
    }

    public enum ParamType {
        EC2_PARAM("ec2Param("),
        ENV_VAR_PARAM("envVar(");

        @Getter
        private final String paramPrefix;

        ParamType(String paramPrefix) {
            this.paramPrefix = paramPrefix;
        }
    }
}

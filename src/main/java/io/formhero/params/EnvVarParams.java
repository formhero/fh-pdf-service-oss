package io.formhero.params;

public class EnvVarParams implements ServiceParams {
    @Override
    public String get(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value != null) {
            return value;
        }

        return defaultValue;
    }

    @Override
    public String get(String key) {
        return get(key, null);
    }
}

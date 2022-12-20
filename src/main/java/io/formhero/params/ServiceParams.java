package io.formhero.params;

public interface ServiceParams {
    String get(String key, String defaultValue);

    String get(String key);
}

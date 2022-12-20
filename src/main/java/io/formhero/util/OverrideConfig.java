package io.formhero.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by stuart.thompson on 2018-07-08.
 */
class OverrideConfig
{
    private static final Logger log = LogManager.getLogger(OverrideConfig.class.getName());

    /**
     * Load the override configuration from file and convert to JSON object.
     *
     * @param path the path to the override
     * @return the configuration JSON object
     */
    JSONObject loadConfig(String path)
    {
        JSONObject jsonConfig = null;
        try {
            // Read configuration and convert to JSON
            InputStream is = new FileInputStream(new File(path).getAbsolutePath());
            JSONObject jsonObject = new JSONObject(new JSONTokener(is));

            // Get config object
            jsonConfig = jsonObject.getJSONObject("config");
        }
        catch (IOException ioe) {
            log.warn("Failed to load file " + path + " - " + ioe.getMessage());
        }
        catch (JSONException je) {
            log.warn("Unable to extract \"config\" from configuration file " + path + " - " + je.getMessage());
        }
        return jsonConfig;
    }

    /**
     * Merge the source into the target configuration.
     * This is done by recursively merging the source JSON into the target JSON.
     *
     * @param source the source JSON object
     * @param target the destination JSON object
     * @return the merged JSON object
     */
     JSONObject mergeConfig(JSONObject source, JSONObject target)
     {
        // Examine all source JSON objects
        for (String key: JSONObject.getNames(source)) {
            Object value = source.get(key);

            // Insert source object to the target
            if (!target.has(key)) {
                target.put(key, value);
            }
            else {
                // Recursively merge a JSON object to the target
                if (value instanceof JSONObject) {
                    JSONObject jsonValue = (JSONObject) value;
                    mergeConfig(jsonValue, (JSONObject) target.get(key));
                }

                // Append the JSON array objects to the target
                else if (value instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) value;
                    for (Object obj: jsonArray) {
                        ((JSONArray) target.get(key)).put(obj);
                    }
                }
                else {
                    // Update target with source object
                    target.put(key, value);
                }
            }
        }
        return target;
    }
}

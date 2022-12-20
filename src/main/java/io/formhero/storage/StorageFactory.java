package io.formhero.storage;

import io.formhero.storage.aws.CachingAwsS3StorageUtil;
import io.formhero.storage.azure.CachingAzureBlobStorageUtil;
//import io.formhero.storage.gae.CachingGoogleCloudStorageUtil;
import io.formhero.util.FhConfig;
import io.formhero.util.FhConfigException;

/**
 * Created by ryan.kimber on 2017-10-03.
 */
public class StorageFactory
{
    // Configuration for template storage
    private static FhConfig fhConfig;

    private static FhConfig getConfig() throws FhConfigException
    {
        if (fhConfig == null) fhConfig = FhConfig.loadConfig();
        return fhConfig;
    }


    /**
     * Delegate to storage provider, as configured in templateStorage config.
     */
    public static StorageProvider getStorageProvider() throws FhConfigException
    {
        // Get template storage configuration
        FhConfig fhConfig = getConfig();
        String storageType = fhConfig.getString("templateStorage.type");

        if (storageType != null) {
            // TODO: add google to templateStorage
            // return new CachingGoogleCloudStorageUtil(fhConfig)
            if ("azure".equalsIgnoreCase(storageType)) {
                return new CachingAzureBlobStorageUtil(fhConfig);
            } else {
                return new CachingAwsS3StorageUtil();
            }
        } else {
            // Default to environment variable config
            return new CachingAwsS3StorageUtil();
        }
    }
}

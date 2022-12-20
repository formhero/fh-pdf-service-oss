package io.formhero.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by ryankimber on 2016-03-12.
 * A tidy little wrapper for a file retrieved from Google Cloud Storage or S3 so that our
 * JCS (Java Cache Service) can serialize it to memory/disk as required.
 *
 * Our goal is to have this service reading files from GCS/S3 as infrequently as possible.
 * As long as we're using good version names or timestamps on the file names, that should work very well.
 */
@AllArgsConstructor
@Getter
@Setter
public class CachedFile implements Serializable {
    private static final long serialVersionUID = 6392376146163510146L;
    private String bucketName;
    private String filePath;
    private byte[] bytes;
}

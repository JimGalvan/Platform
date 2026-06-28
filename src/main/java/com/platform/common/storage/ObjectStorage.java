package com.platform.common.storage;

public interface ObjectStorage {

    void put(String objectKey, byte[] bytes, String contentType);

    String presignedReadUrl(String objectKey);
}

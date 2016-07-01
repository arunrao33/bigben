package com.walmartlabs.components.scheduler.model;

import com.walmart.gmp.ingestion.platform.framework.data.core.Entity;

import java.time.ZonedDateTime;


/**
 * Created by smalik3 on 3/18/16
 */
public interface Bucket extends Entity<ZonedDateTime> {

    enum BucketStatus {PROCESSED, ERROR, UN_PROCESSED, PROCESSING, TIMED_OUT}

    String getStatus();

    void setStatus(String status);

    long getCount();

    void setCount(long count);

    void setProcessedAt(ZonedDateTime date);

    ZonedDateTime getProcessedAt();
}

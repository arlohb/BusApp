package uk.co.arlodev.testapp;

import androidx.annotation.Nullable;

import java.util.Date;

public class Metadata {
    Date responseTimestamp;
    String producerRef;
    String requestMessageRef;
    Date validUntil;
    @Nullable
    String shortestPossibleCycle;
}

package uk.co.arlodev.testapp;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

enum DirectionRef {
    INBOUND,
    OUTBOUND,
}

public class Vehicle {
    Date recordedAtTime;
    @Nullable
    String itemIdentifier;
    Date validUntilTime;

    // Under 'MonitoredVehicleJourney'

    String lineRef;
    DirectionRef directionRef;
    String publishedLineName;
    String operatorRef;
    // Shouldn't be optional, but missing
    @Nullable String originRef;
    // Shouldn't be optional, but missing
    @Nullable String originName;
    String destinationRef;
    @Nullable String destinationName;
    @Nullable Date destinationAimedArrivalTime;
    LatLng vehicleLocation;
    float bearing;
    String blockRef;
    String vehicleJourneyRef;
    String vehicleRef;
}

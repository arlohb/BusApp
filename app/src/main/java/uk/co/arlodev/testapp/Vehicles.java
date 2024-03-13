package uk.co.arlodev.testapp;

import android.util.Log;
import android.util.Xml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Vehicles {
    private final Runnable whenLoaded;

    enum DirectionRef {
        INBOUND,
        OUTBOUND,
    }

    public static class Metadata {
        // Under 'ServiceDelivery'

        Date responseTimestamp;
        String producerRef;

        // Under 'VehicleMonitoringDelivery'

        String requestMessageRef;
        Date validUntil;
        @Nullable String shortestPossibleCycle;
    }

    public static class Vehicle {
        Date recordedAtTime;
        @Nullable String itemIdentifier;
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

    Metadata metadata;
    List<Vehicle> vehicles;

    private void sendRequest() {
        String apiKey = BuildConfig.BUS_API_KEY;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://data.bus-data.dft.gov.uk/api/v1/datafeed/7949/?api_key=" + apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Vehicles", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                if (body == null) {
                    Log.e("Vehicles", "response body is null");
                    return;
                }

                XmlPullParser parser = Xml.newPullParser();
                try {
                    parser.setInput(body.byteStream(), null);
                    parser.require(XmlPullParser.START_DOCUMENT, null, null);
                    parser.next();
                    parser.require(XmlPullParser.START_TAG, null, "Siri");
                    parser.next();
                    parser.require(XmlPullParser.START_TAG, null, "ServiceDelivery");

                    metadata = new Metadata();
                    vehicles = new ArrayList<>();
                    SimpleDateFormat responseTimestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.US);
                    SimpleDateFormat recordedAtTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
                    SimpleDateFormat validUntilTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US);

                    int event;
                    while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                        switch (event) {
                            case XmlPullParser.START_TAG:
                                String tag = parser.getName();
                                switch (tag) {
                                    case "ResponseTimestamp":
                                        parser.next();
                                        metadata.responseTimestamp = responseTimestampFormat.parse(parser.getText());
                                        break;
                                    case "ProducerRef":
                                        parser.next();
                                        metadata.producerRef = parser.getText();
                                        break;
                                    case "VehicleMonitoringDelivery":
                                        parser.next();
                                        while (!"VehicleMonitoringDelivery".equals(parser.getName()))
                                        {
                                            tag = parser.getName();
                                            if (tag == null) { parser.next(); continue; }
                                            switch (tag) {
                                                case "RequestMessageRef":
                                                    parser.next();
                                                    metadata.requestMessageRef = parser.getText();
                                                    break;
                                                case "ValidUntil":
                                                    parser.next();
                                                    if (parser.getText() == null) { parser.next(); continue; }
                                                    metadata.validUntil = responseTimestampFormat.parse(parser.getText());
                                                    break;
                                                case "ShortestPossibleCycle":
                                                    parser.next();
                                                    metadata.shortestPossibleCycle = parser.getText();
                                                    break;
                                                case "VehicleActivity":
                                                    Vehicle vehicle = new Vehicle();
                                                    while ((event = parser.next()) != XmlPullParser.END_TAG
                                                            || !Objects.equals(parser.getName(), "VehicleActivity"))
                                                    {
                                                        if (event != XmlPullParser.START_TAG) continue;
                                                        tag = parser.getName();
                                                        switch (tag) {
                                                            case "RecordedAtTime":
                                                                parser.next();
                                                                vehicle.recordedAtTime = recordedAtTimeFormat.parse(parser.getText());
                                                            case "ItemIdentifier":
                                                                parser.next();
                                                                vehicle.itemIdentifier = parser.getText();
                                                            case "ValidUntilTime":
                                                                parser.next();
                                                                if (parser.getText() == null) { parser.next(); continue; }
                                                                vehicle.validUntilTime = validUntilTimeFormat.parse(parser.getText());
                                                            case "LineRef":
                                                                parser.next();
                                                                vehicle.lineRef = parser.getText();
                                                                break;
                                                            case "DirectionRef":
                                                                parser.next();
                                                                if (parser.getText().equalsIgnoreCase("inbound")) {
                                                                    vehicle.directionRef = DirectionRef.INBOUND;
                                                                } else {
                                                                    vehicle.directionRef = DirectionRef.OUTBOUND;
                                                                }
                                                                break;
                                                            case "PublishedLineName":
                                                                parser.next();
                                                                vehicle.publishedLineName = parser.getText();
                                                                break;
                                                            case "OperatorRef":
                                                                parser.next();
                                                                vehicle.operatorRef = parser.getText();
                                                                break;
                                                            case "OriginRef":
                                                                parser.next();
                                                                vehicle.originRef = parser.getText();
                                                                break;
                                                            case "OriginName":
                                                                parser.next();
                                                                vehicle.originName = parser.getText();
                                                                break;
                                                            case "DestinationRef":
                                                                parser.next();
                                                                vehicle.destinationRef = parser.getText();
                                                                break;
                                                            case "DestinationName":
                                                                parser.next();
                                                                vehicle.destinationName = parser.getText();
                                                                break;
                                                            case "DestinationAimedArrivalTime":
                                                                parser.next();
                                                                vehicle.destinationAimedArrivalTime = recordedAtTimeFormat.parse(parser.getText());
                                                                break;
                                                            case "VehicleLocation":
                                                                parser.next();
                                                                parser.require(XmlPullParser.START_TAG, null, "Longitude");
                                                                parser.next();
                                                                parser.require(XmlPullParser.TEXT, null, null);
                                                                double lng = Double.parseDouble(parser.getText());
                                                                parser.next();
                                                                parser.require(XmlPullParser.END_TAG, null, "Longitude");
                                                                parser.next();
                                                                parser.require(XmlPullParser.START_TAG, null, "Latitude");
                                                                parser.next();
                                                                parser.require(XmlPullParser.TEXT, null, null);
                                                                double lat = Double.parseDouble(parser.getText());
                                                                parser.next();
                                                                parser.require(XmlPullParser.END_TAG, null, "Latitude");
                                                                vehicle.vehicleLocation = new LatLng(lat, lng);
                                                                break;
                                                            case "Bearing":
                                                                parser.next();
                                                                vehicle.bearing = Float.parseFloat(parser.getText());
                                                                break;
                                                            case "BlockRef":
                                                                parser.next();
                                                                vehicle.blockRef = parser.getText();
                                                                break;
                                                            case "VehicleJourneyRef":
                                                                parser.next();
                                                                vehicle.vehicleJourneyRef = parser.getText();
                                                                break;
                                                            case "VehicleRef":
                                                                parser.next();
                                                                vehicle.vehicleRef = parser.getText();
                                                                break;
                                                        }
                                                    }
                                                    vehicles.add(vehicle);
                                                    break;
                                            }
                                            parser.next();
                                        }
                                        break;
                                }
                                break;
                            case XmlPullParser.TEXT:
                            case XmlPullParser.END_TAG:
                                break;
                        }
                    }
                } catch (XmlPullParserException | ParseException e) {
                    Log.e("Vehicles", Log.getStackTraceString(e));
                } finally {
                    body.close();
                }

                int stop = vehicles.size();
                for (int i = 0; i < stop; i++) {
                    Vehicle vehicle = vehicles.get(i);
                    long elapsed_s = vehicle.recordedAtTime.toInstant().until(Instant.now(), ChronoUnit.SECONDS);
                    if (elapsed_s >= 5 * 60) {
                        vehicles.remove(i);
                        i -= 1;
                        stop -= 1;
                    }
                }

                whenLoaded.run();
            }
        });
    }

    public void reload() {
        sendRequest();
    }

    public Vehicles(Runnable whenLoaded) {
        this.whenLoaded = whenLoaded;
        reload();
    }
}

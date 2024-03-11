package uk.co.arlodev.testapp;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Vehicles {
    // TODO make private
    public String xmlStr;
    private final Runnable whenLoaded;

    public void sendRequest() {
        String apiKey = BuildConfig.BUS_API_KEY;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://data.bus-data.dft.gov.uk/api/v1/datafeed/14029/?api_key=" + apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Vehicles", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() == null) {
                    Log.e("Vehicles", "response body is null");
                    return;
                }
                xmlStr = response.body().string();

                whenLoaded.run();
            }
        });
    }

    public Vehicles(Runnable whenLoaded) {
        this.whenLoaded = whenLoaded;
        sendRequest();
    }
}

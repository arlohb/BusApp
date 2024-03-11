package uk.co.arlodev.testapp;

import android.app.Activity;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Vehicles {
    String xmlStr;
    Button button;
    Activity activity;

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
                Log.i("Vehicles", xmlStr);

                activity.runOnUiThread(() -> {
                    button.setText("Done!");
                });
            }
        });
    }

    public Vehicles(Button button, Activity activity) {
        this.button = button;
        this.activity = activity;
        sendRequest();
    }
}

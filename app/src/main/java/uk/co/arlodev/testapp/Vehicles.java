package uk.co.arlodev.testapp;

import java.util.List;

public class Vehicles {
    private final Runnable whenLoaded;

    Metadata metadata;
    List<Vehicle> vehicles;

    VehiclesParser parser = new VehiclesParser();

    public void reload() {
        parser.getData(pair -> {
            vehicles = pair.first;
            metadata = pair.second;

            whenLoaded.run();
        });
    }

    public Vehicles(Runnable whenLoaded) {
        this.whenLoaded = whenLoaded;
        reload();
    }
}

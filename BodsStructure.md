```ts
type XML = {
Siri: {
  ServiceDelivery: {
    ResponseTimestamp: Date, // mandatory
    ProducerRef: string, // mandatory
    VehicleMonitoringDelivery: { // mandatory
      ResponseTimestamp: Date, // mandatory
      RequestMessageRef: string,
      ValidUntil: Date, // mandatory
      ShortestPossibleCycle: string,
      VehicleActivity: {
        RecordedAtTime: Date, // mandatory
        ItemIdentifier: string, // optional
        ValidUntilTime: Date, // mandatory
        MonitoredVehicleJourney: {
          LineRef: string, // mandatory
          DirectionRef: "inbound" | "outbound", // mandatory, (case?)
          PublishedLineName: string, // mandatory
          OperatorRef: string, // mandatory
          OriginRef: string, // mandatory, MISSING
          OriginName: string, // mandatory, MISSING
          DestinationRef: string, // mandatory
          DestinationName: string, // optional
          DestinationAimedArrivalTime: Date, // optional
          VehicleLocation: { // mandatory
            Longitude: number,
            Latitude: number,
          },
          Bearing: number, // mandatory
          BlockRef: string, // mandatory
          VehicleJourneyRef: string, // mandatory
          VehicleRef: string, // mandatory
        },
        Extensions: void,
      } [],
    },
  },
}};
```

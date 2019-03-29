package com.thingworx.sdk.delivery;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.communications.client.things.VirtualThingPropertyChangeEvent;
import com.thingworx.communications.client.things.VirtualThingPropertyChangeListener;
import com.thingworx.metadata.FieldDefinition;
import com.thingworx.metadata.annotations.ThingworxEventDefinition;
import com.thingworx.metadata.annotations.ThingworxEventDefinitions;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.metadata.collections.FieldDefinitionCollection;
import com.thingworx.types.BaseTypes;
import com.thingworx.types.InfoTable;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.constants.CommonPropertyNames;
import com.thingworx.types.primitives.DatetimePrimitive;
import com.thingworx.types.primitives.structs.Location;

//Refer to the "Delivery Truck Example" section of the documentation
//for a detailed explanation of this example's operation 

// Property Definitions
@SuppressWarnings("serial")
@ThingworxPropertyDefinitions(properties = {	
		@ThingworxPropertyDefinition(name="Driver", description="The name of the driver", baseType="STRING", aspects={"isReadOnly:false"}),
		@ThingworxPropertyDefinition(name="DeliveriesLeft", description="The number of deliveries left for this truck", baseType="NUMBER", aspects={"isReadOnly:false"}),
		@ThingworxPropertyDefinition(name="Speed", description="The speed of the truck", baseType="NUMBER", aspects={"isReadOnly:false"}),
		@ThingworxPropertyDefinition(name="Location", description="The location of the truck", baseType="LOCATION", aspects={"isReadOnly:false"}),
		@ThingworxPropertyDefinition(name="TotalDeliveries", description="The number of deliveries the truck has to carry out.", baseType="NUMBER", aspects={"isReadOnly:false"}),
		@ThingworxPropertyDefinition(name="DeliveriesMade", description="The number of deliveries the truck has made.", baseType="NUMBER", aspects={"isReadOnly:false"}),
})

// Event Definitions
@ThingworxEventDefinitions(events = {
	@ThingworxEventDefinition(name="DeliveryStop", description="The event of a delivery truck stopping to deliver a package.", dataShape="DeliveryTruckShape", isInvocable=true, isPropertyEvent=false)
})

// Delivery Truck virtual thing class that simulates a Delivery Truck
public class DeliveryTruckThing extends VirtualThing implements Runnable, VirtualThingPropertyChangeListener {
	private static final Logger LOG = LoggerFactory.getLogger(DeliveryTruckThing.class);
	private Thread _shutdownThread = null;
	private List<String> drivers;
	private double deliveriesMade;
	private double deliveriesLeft;
	private double totalDeliveries;
	private String driver;
	private double speed;
	private Location location;

	private final static String ACTIV_TIME_FIELD = "ActivationTime";
	private final static String DELIVERIES_LEFT_FIELD = "DeliveriesLeft";
	private final static String DELIVERIES_MADE_FIELD = "DeliveriesMade";
	private final static String TOTAL_DELIVERIES_FIELD = "TotalDeliveries";
	private final static String REMAIN_DELIVERIES_FIELD = "RemainingDeliveries";
	private final static String DRIVER_NAME_FIELD = "DriverName";
	private final static String DRIVER_FIELD = "Driver";
	private final static String TRUCK_NAME_FIELD = "Truck";
	private final static String LOCATION_FIELD = "Location";
	private final static String SPEED_FIELD = "Speed";

	public DeliveryTruckThing(String name, String description, ConnectedThingClient client) throws Exception {
		super(name, description, client);

		// Populate the thing shape with the properties, services, and events that are annotated in this code
		super.initializeFromAnnotations();
		this.init();
		this.addPropertyChangeListener(this);
	}

	// From the VirtualThing class
	// This method will get called when a connect or reconnect happens
	// Need to send the values when this happens
	// This is more important for a solution that does not send its properties on a regular basis
	public void synchronizeState() {
		// Be sure to call the base class
		super.synchronizeState();
		// Send the property values to ThingWorx when a synchronization is required
		super.syncProperties();
	}

	private void init() {
		// Data Shape definition that is used by the delivery stop event
		// The event only has one field, the message
        FieldDefinitionCollection fields = new FieldDefinitionCollection();
        fields.addFieldDefinition(new FieldDefinition(ACTIV_TIME_FIELD, BaseTypes.DATETIME));
        fields.addFieldDefinition(new FieldDefinition(DRIVER_NAME_FIELD, BaseTypes.STRING));
        fields.addFieldDefinition(new FieldDefinition(TRUCK_NAME_FIELD, BaseTypes.BOOLEAN));
        fields.addFieldDefinition(new FieldDefinition(TOTAL_DELIVERIES_FIELD, BaseTypes.NUMBER));
        fields.addFieldDefinition(new FieldDefinition(REMAIN_DELIVERIES_FIELD, BaseTypes.NUMBER));
        fields.addFieldDefinition(new FieldDefinition(LOCATION_FIELD, BaseTypes.LOCATION));
        defineDataShapeDefinition("DeliveryTruckShape", fields);

        drivers = new ArrayList<String>();
        drivers.add("Max");
        drivers.add("Mellissa");
        drivers.add("Mathew");
        drivers.add("Megan");
        drivers.add("Merv");
        drivers.add("Michelle");
        drivers.add("Merideth");
        drivers.add("Mona");
        drivers.add("Maxine");

        // If the truck made all of it's deliveries
 		// Send the truck back out
 		if((deliveriesMade >= totalDeliveries) || (deliveriesLeft <= 0)) {
 			System.out.println("Reset Deliveries For " + this.getName() +"!");
 			totalDeliveries = 500d;
 			deliveriesLeft = 500d;
 			deliveriesMade = 0d;
 			driver = drivers.get((int) (0 + 9 * Math.random()));

 			try {
				super.setProperty(TOTAL_DELIVERIES_FIELD, totalDeliveries);
				super.setProperty(DELIVERIES_LEFT_FIELD, deliveriesLeft);
				super.setProperty(DELIVERIES_MADE_FIELD, deliveriesMade);
				super.setProperty(DRIVER_FIELD, driver);
				super.updateSubscribedProperties(10000);
			} catch (Exception e) {
				LOG.error("Failed to write to the ThingWorx composer.");
			}
 		}
 		
 		Double latitude = 40 + 45 * Math.random();
		Double longitude = (70 + 80 * Math.random()) * -1;
		location = new Location(longitude, latitude);
		driver = drivers.get((int) (0 + 9 * Math.random()));
	}

	// The processScanRequest is called by the DeliveryTruckClient every scan cycle
	@Override
	public void processScanRequest() throws Exception {
		// Execute the code for this simulation every scan
		this.scanDevice();
		this.updateSubscribedProperties(1000);
		this.updateSubscribedEvents(1000);
	}

	// Performs the logic for the delivery truck, occurs every scan cycle
	public void scanDevice() throws Exception {
		int counter = (int) (0 + 100000 * Math.random());

		// If the truck made all of it's deliveries
 		// Send the truck back out
 		if((deliveriesMade >= totalDeliveries) || (deliveriesLeft <= 0)) {
 			System.out.println("Reset Deliveries For " + this.getName() +"!");
 			totalDeliveries = 500d;
 			deliveriesLeft = 500d;
 			deliveriesMade = 0d;
 			driver = drivers.get((int) (0 + 9 * Math.random()));

 			try {
				super.setProperty(TOTAL_DELIVERIES_FIELD, totalDeliveries);
				super.setProperty(DELIVERIES_LEFT_FIELD, deliveriesLeft);
				super.setProperty(DELIVERIES_MADE_FIELD, deliveriesMade);
				super.setProperty(DRIVER_FIELD, driver);
			} catch (Exception e) {
				LOG.error("Failed to write to the ThingWorx composer.");
			}
 		}

		if((counter % 3) == 0 || (counter % 5) == 0) { // A truck delivery stop
			System.out.println(this.getName() +" Is Making A Delivery!!");
			// Set the Speed property value if the DeliveriesMade value
			// is equal to zero, raise speed
			// is good enough, lower speed
			if(deliveriesMade == 0){
				// Set the Speed property value in the range of 80-140
				speed = 80 + 140 * Math.random();
				super.setProperty(SPEED_FIELD, speed);
			}
			else {
				// Set the Speed property value in the range of 60-100
				speed = 60 + 100 * Math.random();
				super.setProperty(SPEED_FIELD, speed);
			}

			// Get the last location of the truck 
			// Set location value based on new values
			Double latitude = 40 + 45 * Math.random();
			Double longitude = (70 + 80 * Math.random()) * -1;
			location.setLatitude(latitude);
			location.setLongitude(longitude);

			// Update deliveries
			deliveriesMade++;
			deliveriesLeft--;
			super.setProperty(LOCATION_FIELD, location);
			super.setProperty(DELIVERIES_LEFT_FIELD, deliveriesLeft);
			super.setProperty(DELIVERIES_MADE_FIELD, deliveriesMade);

			// Set the event information of the defined data shape for a truck stop event
			ValueCollection payload = new ValueCollection();

			// Set values to the fields
			payload.SetLocationValue(LOCATION_FIELD, location);
			payload.SetNumberValue(REMAIN_DELIVERIES_FIELD, deliveriesLeft);
			payload.SetDateTimeValue(ACTIV_TIME_FIELD, new DatetimePrimitive(DateTime.now()));
			payload.SetNumberValue(TOTAL_DELIVERIES_FIELD, totalDeliveries);
			payload.SetStringValue(DRIVER_NAME_FIELD, driver);
			payload.SetStringValue(TRUCK_NAME_FIELD, super.getName());

			// This will trigger the 'DeliveryStop' of a remote thing 
			// on the platform.
			super.queueEvent("DeliveryStop", new DateTime(), payload);
		}
		else if((counter % 4) == 0) { // Delivery truck stopped for other reason
			System.out.println(this.getName() +" Has Stopped!");
			// Set the Speed property value to 0
			speed = 0d;
			super.setProperty(SPEED_FIELD, speed);
		}
		else if((counter % 2) == 0) { // Delivery truck running
			System.out.println(this.getName() +" Is Moving!");
			// Set the Speed property value in the range of 0-60
			speed = 0 + 60 * Math.random();
			super.setProperty(SPEED_FIELD, speed);
		}
	}

	@ThingworxServiceDefinition(name="DeliveriesCalc", description="Subtract two numbers to set property")
	@ThingworxServiceResult(name=CommonPropertyNames.PROP_RESULT, description="Result", baseType="NUMBER")
	public Double DeliveriesCalc( 
		@ThingworxServiceParameter( name="totalDeliveries", description="Value 1", baseType="NUMBER") Double totalDeliveries,
		@ThingworxServiceParameter( name="deliveriesMade", description="Value 2", baseType="NUMBER") Double deliveriesMade) throws Exception {

		return totalDeliveries - deliveriesMade;
	}

	@ThingworxServiceDefinition(name="GetBigString", description="Example string service.")
	@ThingworxServiceResult(name=CommonPropertyNames.PROP_RESULT, description="Result", baseType="STRING")
	public String GetBigString() {
		StringBuilder sbValue = new StringBuilder();

		for(int index = 0; index < 24000; index++) {
			sbValue.append('0');
		}

		return sbValue.toString();
	}

	@ThingworxServiceDefinition(name="Shutdown", description="Shutdown service.")
	@ThingworxServiceResult(name=CommonPropertyNames.PROP_RESULT, description="", baseType="NOTHING")
	public synchronized void Shutdown() throws Exception {
		// Should not have to do this, but guard against this method being called more than once.
		if(this._shutdownThread == null) {
			// Create a thread for shutting down and start the thread
			this._shutdownThread = new Thread(this);
			this._shutdownThread.start();
		}
	}

	@ThingworxServiceDefinition(name="GetTruckReadings", description="Get Truck Readings")
	@ThingworxServiceResult(name=CommonPropertyNames.PROP_RESULT, description="Result", baseType="INFOTABLE", aspects={"dataShape:DeliveryTruckShape"})
	public InfoTable GetTruckReadings(String truck, String driver) {		
		InfoTable result = new InfoTable(getDataShapeDefinition("DeliveryTruckShape"));
		ValueCollection entry = new ValueCollection();
		DateTime now = DateTime.now();
		Location location = new Location(40.8447819d, -73.8648268d, 14d);

		try {			
			//entry 1
			entry.clear();
			entry.SetStringValue(DRIVER_NAME_FIELD, driver);
			entry.SetDateTimeValue(ACTIV_TIME_FIELD, now.plusDays(1));
			entry.SetStringValue(TRUCK_NAME_FIELD, truck);
			entry.SetNumberValue(TOTAL_DELIVERIES_FIELD, 521);
			entry.SetNumberValue(REMAIN_DELIVERIES_FIELD, 521);
			entry.SetLocationValue(LOCATION_FIELD, location);
			result.addRow(entry.clone());
			
			location = new Location(40.71499674, -73.95378113d, 4d);

			//entry 2
			entry.clear();
			entry.SetStringValue(DRIVER_NAME_FIELD, driver);
			entry.SetDateTimeValue(ACTIV_TIME_FIELD, now.plusDays(2));
			entry.SetStringValue(TRUCK_NAME_FIELD, truck);
			entry.SetNumberValue(TOTAL_DELIVERIES_FIELD, 521);
			entry.SetNumberValue(REMAIN_DELIVERIES_FIELD, 515);
			entry.SetLocationValue(LOCATION_FIELD, location);
			result.addRow(entry.clone());
			
			location = new Location(40.73685215d, -74.19410706d, 54d);

			//entry 3
			entry.clear();
			entry.SetStringValue(DRIVER_NAME_FIELD, driver);
			entry.SetDateTimeValue(ACTIV_TIME_FIELD, now.plusDays(3));
			entry.SetStringValue(TRUCK_NAME_FIELD, truck);
			entry.SetNumberValue(TOTAL_DELIVERIES_FIELD, 521);
			entry.SetNumberValue(REMAIN_DELIVERIES_FIELD, 500);
			entry.SetLocationValue(LOCATION_FIELD, location);
			result.addRow(entry.clone());
			
			location = new Location(41.02549938d, -73.64341736d, 43d);

			//entry 4
			entry.clear();
			entry.SetStringValue(DRIVER_NAME_FIELD, driver);
			entry.SetDateTimeValue(ACTIV_TIME_FIELD, now.plusDays(4));
			entry.SetStringValue(TRUCK_NAME_FIELD, truck);
			entry.SetNumberValue(TOTAL_DELIVERIES_FIELD, 521);
			entry.SetNumberValue(REMAIN_DELIVERIES_FIELD, 440);
			entry.SetLocationValue(LOCATION_FIELD, location);
			result.addRow(entry.clone());
			
			location = new Location(40.91662589d, -72.66700745d, 3d);

			//entry 5
			entry.clear();
			entry.SetStringValue(DRIVER_NAME_FIELD, driver);
			entry.SetDateTimeValue(ACTIV_TIME_FIELD, now.plusDays(5));
			entry.SetStringValue(TRUCK_NAME_FIELD, truck);
			entry.SetNumberValue(TOTAL_DELIVERIES_FIELD, 521);
			entry.SetNumberValue(REMAIN_DELIVERIES_FIELD, 315);
			entry.SetLocationValue(LOCATION_FIELD, location);
			result.addRow(entry.clone());
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public void run() {
		try {
			this.processScanRequest();
		} catch (Exception x) {
			// Not much can be done if there is an exception here
			// In the case of production code should at least log the error
		}
	}

	@Override
	public void propertyChangeEventReceived(VirtualThingPropertyChangeEvent event) {
		if (DELIVERIES_MADE_FIELD.equals(event.getPropertyDefinition().getName())) {
			this.deliveriesMade = (double) event.getPrimitiveValue().getValue();
		} else if(DELIVERIES_LEFT_FIELD.equals(event.getPropertyDefinition().getName())){
			this.deliveriesLeft = (double) event.getPrimitiveValue().getValue();
		} else if(TOTAL_DELIVERIES_FIELD.equals(event.getPropertyDefinition().getName())){
			this.totalDeliveries = (double) event.getPrimitiveValue().getValue();
		} else if(DRIVER_FIELD.equals(event.getPropertyDefinition().getName())){
			this.driver = event.getPrimitiveValue().getStringValue();
		} else if(SPEED_FIELD.equals(event.getPropertyDefinition().getName())){
			this.speed = (double) event.getPrimitiveValue().getValue();
		} else if(LOCATION_FIELD.equals(event.getPropertyDefinition().getName())){
			this.location = (Location) event.getPrimitiveValue().getValue();
		}
	}
}

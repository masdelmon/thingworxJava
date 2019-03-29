package com.thingworx.sdk.simplething;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.relationships.RelationshipTypes.ThingworxEntityTypes;
import com.thingworx.types.InfoTable;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.primitives.LocationPrimitive;
import com.thingworx.types.primitives.structs.Location;

public class SimpleThingClient extends ConnectedThingClient {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleThingClient.class);
	private static String ThingName = "SimpleThing_1";
	private static String property = "count";

	public SimpleThingClient(ClientConfigurator config) throws Exception {
		super(config);
	}

	public static void main(String[] args) {
		ClientConfigurator config = new ClientConfigurator();

		// Set the URI of the server that we are going to connect to.
		// You must include the port number in the URI.
		// The first example below is used for http connections
		// Use example 2 for more secure https connections
		//URI Example 1: ws://<host>:80/Thingworx/WS");
		//URI Example 2: wss://<host>:443/Thingworx/WS");
		config.setUri("wss://localhost:443/Thingworx/WS");

		// Set the Application Key. This will allow the client to authenticate with the server.
		// It will also dictate what the client is authorized to do once connected.
		// The below application key is provided. Replace with your own application key to be more secure
		config.setAppKey("ce22e9e4-2834-419c-9656-ef9f844c784c");

		// This will allow us to test against a server using a self-signed certificate.
		// This should be removed for production systems.
		config.ignoreSSLErrors(true); // All self signed certs

		try {
			// Create our client.
			SimpleThingClient client = new SimpleThingClient(config);

			// Start the client. The client will connect to the server and authenticate
			// using the ApplicationKey specified above.
			client.start();

			// Lets wait to get connected
			LOG.info("****************Connecting to ThingWorx Server****************");
						
			// Wait for the client to connect.
			if (client.waitForConnection(30000)) {
				LOG.info("****************Connected to ThingWorx Server****************");
				// Reading a property. Not using binding
				///////////////////////////////////////////////////////////////

				// Request a property from a Thing on the Platform. Here we access the 'count'
				// property of a Thing while NOT using binding.
				InfoTable result = client.readProperty(ThingworxEntityTypes.Things, ThingName, property, 10000);

				// Result is returned as an InfoTable, so we must extract the value. An InfoTable
				// is a collection of one or more rows. A row can have multiple fields. Each 
				// field has a name and a base type. In this case, the field name is 'count' and
				// the base type is INTEGER, so we can use the getValue() helper.
				Integer count = (Integer) result.getFirstRow().getValue(property);

				LOG.info("The count of the Thing {} is: {}", ThingName, count);

				// We can also access the value as a Primitive. This will work for all primitive types.
				int prim = (int) result.getFirstRow().getPrimitive(property).getValue();

				LOG.info("The count of the Thing {} is: {}. This is based on using the Primitive class.", ThingName, prim);

				// Writing a property. Not using binding
				///////////////////////////////////////////////////////////////

				Location location = new Location(42.36, -71.06, 10.0);

				// This will set the location property of the Thing to the GPS 
				// coordinates of Boston, MA.
				client.writeProperty(ThingworxEntityTypes.Things, ThingName, "location", new LocationPrimitive(location), 5000);

				LOG.info("Wrote to the property 'location' of Thing {}. value: {}", ThingName, location.toString());

				// Invoking a service on a Thing
				///////////////////////////////////////////////////////////////

				// A ValueCollection is used to specify a service's parameters
				ValueCollection params = new ValueCollection();
				params.SetStringValue("name", "SimpleThing_2");
				params.SetStringValue("description", "A new Thing");
				params.SetStringValue("thingTemplateName", "RemoteThing");

				// Use the SimpleThing_1 Thing to create a new Thing on the Platform.
				// This service's result type is NOTHING, so we can ignore the response.
				try{
					client.invokeService(ThingworxEntityTypes.Things, ThingName, "CreateNewThing", params, 5000);
				} catch (Exception e) {
					LOG.error("An exception occured while initializing the client", e);

					//Probably ran this code multiple times by now
					//This might be saying that you can't create a new thing because
					//You created it already. Let's run the delete then create it again
					client.invokeService(ThingworxEntityTypes.Things, ThingName, "DeleteThing", params, 5000);
					client.invokeService(ThingworxEntityTypes.Things, ThingName, "CreateNewThing", params, 5000);
				}

				// Firing an event
				///////////////////////////////////////////////////////////////
				
				// A ValueCollection is used to specify a event's payload
				ValueCollection payload = new ValueCollection();
				
				payload.SetStringValue("Name", "Test");
				payload.SetIntegerValue("Count", 5);
				payload.SetLocationValue("Location", location);

				// This will trigger the 'ExampleEvent' of a RemoteThing on the Platform.
				client.fireEvent(ThingworxEntityTypes.Things, ThingName, "ExampleEvent", payload, 5000);

				// Create a VirtualThing and bind it to the client
				///////////////////////////////////////////////////////////////

				// Create a new VirtualThing. The name parameter should correspond with the 
				// name of a RemoteThing on the Platform.
				SimpleThing thing = new SimpleThing("SimpleThing_2", "A virtual thing", client);

				// Bind the VirtualThing to the client. This will tell the Platform that
				// the RemoteThing 'SimpleThing_2' is now connected and that it is ready to 
				// receive requests.
				client.bindThing(thing);
			} else {
				// Log this as a warning. In production the application could continue
				// to execute, and the client would attempt to reconnect periodically.
				LOG.warn("Client did not connect within 30 seconds.");
				LOG.warn("Check connection configurations and the ThingWorx logs. Exiting");
			}

			client.shutdown();
		} catch (Exception e) {
			LOG.error("An exception occured while initializing the client", e);
		}

		LOG.info("SimpleThingClient is done. Exiting");
	}
}

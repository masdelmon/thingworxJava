package com.thingworx.sdk.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;

public class ExampleClient extends ConnectedThingClient {

	private static final Logger LOG = LoggerFactory.getLogger(ExampleClient.class);
	
	private static String ThingName = "SimpleThing_1";

	public ExampleClient(ClientConfigurator config) throws Exception {
		super(config);
	}

	public static void main(String[] args) {
		ClientConfigurator config = new ClientConfigurator();
		boolean firstScan = true;

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
		config.setAppKey("b3d06be7-c9e1-4a9c-b967-28cd4c49fa80");

		// This will allow us to test against a server using a self-signed certificate.
		// This should be removed for production systems.
		config.ignoreSSLErrors(true); // All self signed certs

		try {
			// Create our client that will communication with the ThingWorx composer.
			ExampleClient client = new ExampleClient(config);

			// Create a new VirtualThing. The name parameter should correspond with the 
			// name of a RemoteThing on the Platform. In this example, the SimpleThing_1 is used.
			VirtualThing thing = new VirtualThing(ThingName, "A basic virtual thing", client);

			// Bind the VirtualThing to the client. This will tell the Platform that
			// the RemoteThing 'SimpleThing_1' is now connected and that it is ready to 
			// receive requests.
			client.bindThing(thing);

			// Start the client. The client will connect to the server and authenticate
			// using the ApplicationKey specified above.
			client.start();

			// Lets wait to get connected
			LOG.debug("****************Connecting to ThingWorx Server****************");

			// This will prevent the main thread from exiting. It will be up to another thread
			// of execution to call client.shutdown(), allowing this main thread to exit.
			while (!client.isShutdown()) {
				if (client.isConnected()) {
					if(firstScan) {
						LOG.debug("****************Connected to ThingWorx Server****************");
						firstScan = false;
					}

					// Every 15 seconds we tell the thing to process a scan request. This is
					// an opportunity for the thing to query a data source, update property
					// values, and push new property values to the server.

					// This loop demonstrates how to iterate over multiple VirtualThings
					// that have bound to a client. In this simple example the things
					// collection only contains one VirtualThing.
					for (VirtualThing virtualThing : client.getThings().values()) {
						virtualThing.processScanRequest();
					}
				}

				Thread.sleep(15000);
			}
		} catch (Exception e) {
			LOG.error("An exception occured during execution.", e);
		}

		LOG.info("ExampleClient is done. Exiting");
	}
}

package com.thingworx.sdk.examples;

import com.thingworx.types.collections.ValueCollection;

/**
 * A helper class that simplifies code in the FileTransferExample. This class provides static methods to create 
 * virtual directories in the SystemRepository thing in the ThingWorx composer, creates a file located in the SystemRepository
 *  thing, and creates the virtual directories in a RemoteThingWithFileTransfer thing that is provided to the method.
 */
public class FileTransferExampleHelper {
	/**
	 * A helper method to create the INCOMING path for the SystemRepository thing in the ThingWorx composer. 
	 * This method will be used to provide the SystemRepository thing with a location to RECEIVE new files.
	 * Incoming is based on the SystemRepository thing's perspective.
	 * 
	 * @return The value collection used to provide the SystemRepository thing with a path to receive files from the client application.
	 * @throws Exception 
	 */
	public static ValueCollection createSystemRepositoryIncomingPath() throws Exception{
		// Create the payload for the directory that will accept files from the client application
		ValueCollection inPayload = new ValueCollection();
		inPayload.SetStringValue("path", "incoming");
		return inPayload;
	}

	/**
	 * A helper method to create the OUTGOING path for the SystemRepository thing in the ThingWorx composer. 
	 * This method will be used to provide the SystemRepository thing with a location to SEND new files.
	 * Outgoing is based on the SystemRepository thing's perspective.
	 * 
	 * @return The value collection used to provide the SystemRepository thing with a path to send the client application files
	 * and a newly created file that will be sent to the client.
	 * @throws Exception 
	 */
	public static ValueCollection createSystemRepositoryOutgoingPath() throws Exception{
		// Create the payload for the directory that will send files to the client application
		// This payload will also contain the information to create a file on the SystemRepository thing.
		ValueCollection outPayload = new ValueCollection();
		outPayload.SetStringValue("path", "outgoing/incoming.txt");
		outPayload.SetStringValue("data", "Hello. This is a file coming from the ThingWorx platform.");
		return outPayload;
	}

	/**
	 * A helper method to use the INCOMING path for the provided RemoteThingWithFileTransfer thing (ThingName) in the ThingWorx composer. 
	 * This method will be used to provide the thing/client application with a location to RECEIVE new files. It will also establish the parameters used to 
	 * transfer a file from the SystemRepository to the thing provided (ThingName). Incoming is based on the perspective of the ThingName 
	 * and the client application.
	 * 
	 * @param thingName The name of the thing in which this payload will be created for
	 * 
	 * @return The value collection used to enable the RemoteThingWithFileTransfer thing provided with a path to receive files
	 *  from the ThingWorx composer and set the parameters needed to transfer a file.
	 * @throws Exception 
	 */
	public static ValueCollection createTransferIncomingParameters(String thingName) throws Exception{
		//Create the payload for the file transfer coming to the client application from the ThingWorx composer
		ValueCollection inPayload = new ValueCollection();
		inPayload.SetStringValue("sourceRepo", "SystemRepository");
		inPayload.SetStringValue("sourcePath", "/outgoing");
		inPayload.SetStringValue("sourceFile", "example.txt");
		inPayload.SetStringValue("targetRepo", thingName);
		inPayload.SetStringValue("targetPath", "in");
		inPayload.SetStringValue("targetFile", "example.txt");
		inPayload.SetIntegerValue("timeout", 15000);
		inPayload.SetBooleanValue("async", false);
		return inPayload;
	}

	/**
	 * A helper method to use the OUTGOING path for the provided RemoteThingWithFileTransfer thing (ThingName) in the ThingWorx composer. 
	 * This method will be used to provide the thing/client application with a location to SEND new files to. It will also establish the parameters used to 
	 * transfer a file from the thing provided (ThingName) to the SystemRepository thing. Outgoing is based on the perspective of the ThingName 
	 * and the client application.
	 * 
	 * @param thingName The name of the thing in which this payload will be created for
	 * 
	 * @return The value collection used to enable the RemoteThingWithFileTransfer thing provided with a path to send files
	 *  to the ThingWorx composer and set the parameters needed to transfer a file. 
	 * @throws Exception 
	 */
	public static ValueCollection createTransferOutgoingParameters(String thingName) throws Exception{
		//Create the payload for the file transfer going to the ThingWorx composer from the client application
		ValueCollection outPayload = new ValueCollection();
		outPayload.SetStringValue("sourceRepo", thingName);
		outPayload.SetStringValue("sourcePath", "out");
		outPayload.SetStringValue("sourceFile", "outgoing.txt");
		outPayload.SetStringValue("targetRepo", "SystemRepository");
		outPayload.SetStringValue("targetPath", "/incoming");
		outPayload.SetStringValue("targetFile", "outgoing.txt");
		outPayload.SetIntegerValue("timeout", 15000);
		outPayload.SetBooleanValue("async", false);
		return outPayload;
	}
}

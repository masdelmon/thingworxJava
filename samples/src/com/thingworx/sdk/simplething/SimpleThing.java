package com.thingworx.sdk.simplething;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.metadata.EventDefinition;
import com.thingworx.metadata.FieldDefinition;
import com.thingworx.metadata.PropertyDefinition;
import com.thingworx.metadata.ServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.metadata.collections.FieldDefinitionCollection;
import com.thingworx.types.BaseTypes;
import com.thingworx.types.InfoTable;
import com.thingworx.types.collections.AspectCollection;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.constants.Aspects;
import com.thingworx.types.constants.CommonPropertyNames;
import com.thingworx.types.constants.DataChangeType;
import com.thingworx.types.primitives.LocationPrimitive;
import com.thingworx.types.primitives.StringPrimitive;
import com.thingworx.types.primitives.structs.Location;

@SuppressWarnings("serial")
@ThingworxPropertyDefinitions(properties = {
		@ThingworxPropertyDefinition(name="StringIndex", description="String Index Property", baseType="INFOTABLE"),
		@ThingworxPropertyDefinition(name="Index", description="Integer value", baseType="INTEGER")
})
public class SimpleThing extends VirtualThing {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleThing.class);
	private static final String PROPERTY = "Property1";
	private static final String EVENT = "Event1";
	private static final String SERVICE = "Service1";

	/**
	 * A custom constructor. We implement this so we can call initializeFromAnnotations,
	 * which processes all of the VirtualThing's annotations and applies them to the
	 * object. Also creates a number of properties and definitions using code examples.
	 * 
	 * @param name The name of the thing.
	 * @param description A description of the thing.
	 * @param client The client that this thing is associated with.
	 * @throws Exception 
	 */
	public SimpleThing(String name, String description, ConnectedThingClient client) throws Exception {
		// Call the super class's constrcutor
		super(name, description, client);
		// Call the initializeFromAnnotations method to initialize all of the properties, services, and definitions created from annotations.
		super.initializeFromAnnotations();

		//Create the PROPERTY definition with name, description, and baseType
		PropertyDefinition property1 = new PropertyDefinition(PROPERTY, "Description for Property1", BaseTypes.BOOLEAN);
		//Create an aspect collection to hold all of the different aspects
		AspectCollection aspects = new AspectCollection();
		//Add the dataChangeType aspect
		aspects.SetStringValue(Aspects.ASPECT_DATACHANGETYPE, DataChangeType.NEVER.name());
		//Add the dataChangeThreshold aspect
		aspects.SetNumberValue(Aspects.ASPECT_DATACHANGETHRESHOLD, 0d);
		//Add the cacheTime aspect
		aspects.SetIntegerValue(Aspects.ASPECT_CACHETIME, 0);
		//Add the isPersistent aspect
		aspects.SetBooleanValue(Aspects.ASPECT_ISPERSISTENT, false);
		//Add the isReadOnly aspect
		aspects.SetBooleanValue(Aspects.ASPECT_ISREADONLY, false);
		//Add the pushType aspect
		aspects.SetStringValue("pushType", DataChangeType.NEVER.name());
		//Add the defaultValue aspect
		aspects.SetBooleanValue(Aspects.ASPECT_DEFAULTVALUE, true);
		//Set the aspects of the PROPERTY definition
		property1.setAspects(aspects);
		//Add the PROPERTY definition to the Virtual Thing
		super.defineProperty(property1);

		//Create the EVENT definition with name and description
		EventDefinition event1 = new EventDefinition(EVENT, "Description for Event1");
		//Set the EVENT data shape
		event1.setDataShapeName("SimpleDataShape");
		//Set remote access
		event1.setLocalOnly(false);
		//Add the EVENT definition to the Virtual Thing
		super.defineEvent(event1);

		//Create the SERVICE definition with name and description
		ServiceDefinition service1 = new ServiceDefinition(SERVICE, "Description for Service1");
		//Create the input parameter to string parameter 'name'
		FieldDefinitionCollection fields = new FieldDefinitionCollection();
        fields.addFieldDefinition(new FieldDefinition("name", BaseTypes.STRING));
        service1.setParameters(fields);
        //Set remote access
        service1.setLocalOnly(false);
		//Set return type
        service1.setResultType(new FieldDefinition(CommonPropertyNames.PROP_RESULT, BaseTypes.STRING));
        //Add the SERVICE definition to the Virtual Thing
		super.defineService(service1);

        // First a DataShapeDefinition needs to be added to the VirtualThing
        FieldDefinitionCollection infoFields = new FieldDefinitionCollection();
        // Define the fields
        infoFields.addFieldDefinition(new FieldDefinition("ID", BaseTypes.INTEGER));
        infoFields.addFieldDefinition(new FieldDefinition("Value", BaseTypes.STRING));
        // Add the DataShapeDefinition to the VirtualThing
		super.defineDataShapeDefinition("StringMap", infoFields);
	}

	// Service Definition
	@ThingworxServiceDefinition(
	name="StringMapService",
	description="Returns the passed in String Map")
	@ThingworxServiceResult(name=CommonPropertyNames.PROP_RESULT, baseType="INFOTABLE", description="", aspects={"dataShape:StringMap"})
	public InfoTable StringMapService(@ThingworxServiceParameter(name="value", baseType="INFOTABLE", description="", aspects={"dataShape:StringMap"}) InfoTable value ) throws Exception {
		return value;
	}

	//Service1 Definition
	public String Service1(String name) throws Exception {
		String result = "Hello " + name;
		return result;
	}

	/**
	 * This method provides a common interface amongst VirtualThings for processing
	 * periodic requests. It is an opportunity to access data sources, update 
	 * PROPERTY values, push new values to the server, and take other actions.
	 */
	@Override
	public void processScanRequest() {
		try {
			ValueCollection payload = new ValueCollection();
			payload.SetIntegerValue("Count", 5);
			payload.SetLocationValue("Location", new LocationPrimitive(new Location(50d, -100d, 10d)));
			payload.SetStringValue("Name", "Latest");

			super.setProperty(PROPERTY, "Hello There");
			super.queueEvent("SimpleEvent", new DateTime(), payload);

			super.updateSubscribedProperties(1000);
			super.updateSubscribedEvents(1000);
		} catch (Exception e) {
			// This will occur if we provide an unknown PROPERTY name.
			LOG.error("Exception occured while updating properties.", e);
		}
	}

	public String callService(String name) throws Exception{
		ValueCollection payload = new ValueCollection();
		payload.put("name", new StringPrimitive(name));
		InfoTable table = handleServiceRequest(SERVICE, payload);
		return table.getFirstRow().getStringValue("name");
	}

	public String getProperty1() {
		return getProperty(PROPERTY).getValue().getStringValue();
	}
}

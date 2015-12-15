package io.github.u2ware.integration.bacnet.core;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.RemoteObject;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.service.confirmed.ReinitializeDeviceRequest.ReinitializedStateOfDevice;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.Choice;
import com.serotonin.bacnet4j.type.constructed.DateTime;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.Sequence;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.TimeStamp;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.EventType;
import com.serotonin.bacnet4j.type.enumerated.MessagePriority;
import com.serotonin.bacnet4j.type.enumerated.NotifyType;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class BacnetScanner implements DeviceEventListener{

	//private Log logger = LogFactory.getLog(getClass());
	
    public static void main(String[] args) throws Exception {
        IpNetwork network = new IpNetwork();
        Transport transport = new DefaultTransport(network);
        //        transport.setTimeout(15000);
        //        transport.setSegTimeout(15000);
        LocalDevice localDevice = new LocalDevice(1234, transport);
        try {
            localDevice.initialize();
            localDevice.getEventHandler().addListener(new BacnetScanner());
            localDevice.sendGlobalBroadcast(new WhoIsRequest());

            //localDevice.findRemoteDevice(address, deviceId)
            
            Thread.sleep(20000);
            
        }
        finally {
            localDevice.terminate();
        }
    }

	@Override
	public void listenerException(Throwable e) {
		System.out.println("listenerException");
	}

	@Override
	public void iAmReceived(RemoteDevice d) {
		System.out.println("iAmReceived ");
		System.out.println("\t"+d);
		System.out.println("\t"+d.getAddress());
		System.out.println("\t"+d.getAddress().getDescription());
		System.out.println("\t"+d.getAddress().getMacAddress());
		System.out.println("\t"+d.getAddress().getMacAddress().getDescription());
//		System.out.println("\t"+d.getName());
//		System.out.println("\t"+d.getModelName());
//		System.out.println("\t"+d.getVendorId());
//		System.out.println("\t"+d.getVendorName());
		
		System.out.println("\t"+d.getInstanceNumber());
		System.out.println("\t"+d.getObjectIdentifier());
		System.out.println("\t"+d.getObjectIdentifier().getInstanceNumber());
		System.out.println("\t"+d.getObjectIdentifier().getObjectType());

		System.out.println("\t"+d.getAddress());
		System.out.println("\t"+d.getAddress().getDescription());
	
	}

	@Override
	public boolean allowPropertyWrite(Address from, BACnetObject obj, PropertyValue pv) {
		System.out.println("allowPropertyWrite");
		return true;
	}

	@Override
	public void propertyWritten(Address from, BACnetObject obj, PropertyValue pv) {
		System.out.println("propertyWritten");
	}

	@Override
	public void iHaveReceived(RemoteDevice d, RemoteObject o) {
		System.out.println("iHaveReceived");
	}

	@Override
	public void covNotificationReceived(
			UnsignedInteger subscriberProcessIdentifier,
			RemoteDevice initiatingDevice,
			ObjectIdentifier monitoredObjectIdentifier,
			UnsignedInteger timeRemaining,
			SequenceOf<PropertyValue> listOfValues) {
		System.out.println("covNotificationReceived");
	}

	@Override
	public void eventNotificationReceived(UnsignedInteger processIdentifier,
			RemoteDevice initiatingDevice,
			ObjectIdentifier eventObjectIdentifier, TimeStamp timeStamp,
			UnsignedInteger notificationClass, UnsignedInteger priority,
			EventType eventType, CharacterString messageText,
			NotifyType notifyType, Boolean ackRequired, EventState fromState,
			EventState toState, NotificationParameters eventValues) {
		System.out.println("eventNotificationReceived");
	}

	@Override
	public void textMessageReceived(RemoteDevice textMessageSoutceDevice,
			Choice messageClass, MessagePriority messagePriority,
			CharacterString message) {
		System.out.println("textMessageReceived");
	}

	@Override
	public void privateTransferReceived(Address from, UnsignedInteger vendorId,
			UnsignedInteger serviceNumber, Sequence serviceParameters) {
		System.out.println("privateTransferReceived");
	}

	@Override
	public void reinitializeDevice(Address from,
			ReinitializedStateOfDevice reinitializedStateOfDevice) {
		System.out.println("reinitializeDevice");
	}

	@Override
	public void synchronizeTime(Address from, DateTime dateTime, boolean utc) {
		System.out.println("synchronizeTime");
		
	}
	
	
}

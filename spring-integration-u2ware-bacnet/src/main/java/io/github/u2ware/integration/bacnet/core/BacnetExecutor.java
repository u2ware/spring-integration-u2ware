package io.github.u2ware.integration.bacnet.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ClassUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.RemoteObject;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkUtils;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.service.confirmed.ReinitializeDeviceRequest.ReinitializedStateOfDevice;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.BACnetError;
import com.serotonin.bacnet4j.type.constructed.Choice;
import com.serotonin.bacnet4j.type.constructed.DateTime;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.Sequence;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.TimeStamp;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.EventType;
import com.serotonin.bacnet4j.type.enumerated.MessagePriority;
import com.serotonin.bacnet4j.type.enumerated.NotifyType;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;

/**
 * Bundles common core logic for the Bacnet components.
 *
 * @author kslee@u2ware.com
 * @since 1.0.0
 *
 */
@SuppressWarnings("serial")
public class BacnetExecutor extends ThreadPoolTaskExecutor {

	private Log logger = LogFactory.getLog(getClass());
	
	protected int localPort = IpNetwork.DEFAULT_PORT; //47808;
	protected String remoteAddress;
	protected int remoteInstanceNumber;
	private LocalDevice localDevice ;
	private Transport transport;
	private IpNetwork network;
	
	public int getLocalPort() {
		return localPort;
	}
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}
	public String getRemoteAddress() {
		return remoteAddress;
	}
	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	public int getRemoteInstanceNumber() {
		return remoteInstanceNumber;
	}
	public void setRemoteInstanceNumber(int remoteInstanceNumber) {
		this.remoteInstanceNumber = remoteInstanceNumber;
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		
		//int localPort = IpNetwork.DEFAULT_PORT - bacnetDeviceNumber;
		
        network = new IpNetwork(IpNetwork.DEFAULT_BROADCAST_IP, localPort);
        transport = new DefaultTransport(network);
        //        transport.setTimeout(15000);
        //        transport.setSegTimeout(15000);
        
        
        localDevice = new LocalDevice(localPort, transport);
        
        
		try {
			localDevice.initialize();
	        localDevice.getEventHandler().addListener(new DeviceEventListenerImpl());
	        logger.info("BACNet Master Initialized Port Number: "+localPort);
		
		} catch (Exception e) {
			throw new RuntimeException("BACNet Master Error : "+localPort +" "+e.getMessage());
		}
	}

	@Override
	public void destroy()  {
		localDevice.terminate();
        logger.info("BACNet Master Terminated Port Number: "+localPort);
		super.destroy();
	}	
	
	public List<RemoteDevice> getRemoteDevices() {
		List<RemoteDevice> result = Lists.newArrayList(localDevice.getRemoteDevices());
		Collections.sort(result, new Comparator<RemoteDevice>() {
			@Override
			public int compare(RemoteDevice o1, RemoteDevice o2) {
				return o1.getInstanceNumber() > o2.getInstanceNumber() ? 1 : -1;
			}
		});
		return result;
	}

	public void sendGlobalBroadcast(){
	    super.execute(new Runnable() {
			public void run() {
		        localDevice.sendGlobalBroadcast(new WhoIsRequest());
		        logger.info("sendGlobalBroadcast");
			}
		});
	}

	///////////////////////
	//
	///////////////////////
	private RemoteDevice findRemoteDevice(final Address address, final int instanceNumber) throws Exception{
		return super.submit(new Callable<RemoteDevice>() {
			@Override
			public RemoteDevice call() throws Exception {
				RemoteDevice d = localDevice.findRemoteDevice(address, instanceNumber);
		        logger.info("findRemoteDevice: "+d.getAddress().getDescription()+"["+d.getInstanceNumber()+"]");
		        return d;
			}
	    }).get();
	}
	
	private Collection<ObjectIdentifier> sendReadPropertyAllowNull(final RemoteDevice d) throws Exception {
		return super.submit(new Callable<Collection<ObjectIdentifier>>() {
			@Override @SuppressWarnings("unchecked")
			public Collection<ObjectIdentifier> call() throws Exception {
				Collection<ObjectIdentifier> oids = ((SequenceOf<ObjectIdentifier>) 
		        		RequestUtils.sendReadPropertyAllowNull(localDevice, d, d.getObjectIdentifier(), PropertyIdentifier.objectList))
		        		.getValues(); 
		        logger.info("sendReadPropertyAllowNull: "+d.getAddress().getDescription()+"["+d.getInstanceNumber()+"]");
				return oids;
			}
	    }).get();
	}
	
	private PropertyReferences createPropertyReferences(Collection<ObjectIdentifier> oids){
		PropertyReferences refs = new PropertyReferences();
		
		ObjectType d = new ObjectType(384);
		
        for (ObjectIdentifier oid : oids){
        	
        	if( ! d.equals(oid.getObjectType())){
            	
                refs.add(oid, PropertyIdentifier.objectIdentifier);
                refs.add(oid, PropertyIdentifier.objectName);
                refs.add(oid, PropertyIdentifier.objectType);
                refs.add(oid, PropertyIdentifier.presentValue);

                
                ObjectType type = oid.getObjectType();
                if (ObjectType.accumulator.equals(type)) {
                    refs.add(oid, PropertyIdentifier.units);
                }
                else if (ObjectType.analogInput.equals(type) || ObjectType.analogOutput.equals(type)
                        || ObjectType.analogValue.equals(type) || ObjectType.pulseConverter.equals(type)) {
                    refs.add(oid, PropertyIdentifier.units);
                }
                else if (ObjectType.binaryInput.equals(type) || ObjectType.binaryOutput.equals(type)
                        || ObjectType.binaryValue.equals(type)) {
                    refs.add(oid, PropertyIdentifier.inactiveText);
                    refs.add(oid, PropertyIdentifier.activeText);
                }
                else if (ObjectType.lifeSafetyPoint.equals(type)) {
                    refs.add(oid, PropertyIdentifier.units);
                }
                else if (ObjectType.loop.equals(type)) {
                    refs.add(oid, PropertyIdentifier.outputUnits);
                }
                else if (ObjectType.multiStateInput.equals(type) || ObjectType.multiStateOutput.equals(type)
                        || ObjectType.multiStateValue.equals(type)) {
                    refs.add(oid, PropertyIdentifier.stateText);
                }
        	}
        }
        return refs;
	}
	private PropertyValues readProperties(final RemoteDevice d, final PropertyReferences refs) throws Exception{		
		return super.submit(new Callable<PropertyValues>() {
			@Override
			public PropertyValues call() throws Exception {
				PropertyValues pvs = RequestUtils.readProperties(localDevice, d, refs, null);
		        logger.info("readProperties: "+d.getAddress().getDescription()+"["+d.getInstanceNumber()+"]");
				return pvs;
			}
	    }).get();
	}
	
	////////////////////////////////////
	//
	////////////////////////////////////
	public List<BacnetResponse> execute(BacnetRequest request) throws Exception{
		if(BacnetRequest.READ_TYPE.equals(request.getType())){
			return readValues(getRemoteAddress(), getRemoteInstanceNumber());
		}else{
			return null;
		}
	}
	public List<BacnetResponse> readValues() throws Exception{
		return readValues(getRemoteAddress(), getRemoteInstanceNumber());
	}
	public List<BacnetResponse> readValues(final String remoteAddress, final int remoteInstanceNumber) throws Exception{		
		Address address = new Address(IpNetworkUtils.toOctetString(remoteAddress));
		RemoteDevice remoteDevice = localDevice.getRemoteDevice(address);
		if(remoteDevice == null){
			remoteDevice = findRemoteDevice(address, remoteInstanceNumber);
		}
		Collection<ObjectIdentifier> oids = sendReadPropertyAllowNull(remoteDevice);
		PropertyReferences refs = createPropertyReferences(oids);
		PropertyValues pvs = readProperties(remoteDevice, refs);
		
		List<BacnetResponse> result = readValues(pvs, remoteDevice);
		return result;
	}
	
	
	

	//////////////////////////
	//
	//////////////////////////
	private List<BacnetResponse> readValues(PropertyValues pvs, RemoteDevice remoteDevice){
		Map<Object, BacnetResponse> responseMap = Maps.newHashMap();
        
        for (ObjectPropertyReference opr : pvs) {

        	Object value = pvs.getNoErrorCheck(opr);//.toString();
        	
        	if(! ClassUtils.isAssignableValue(BACnetError.class, value)){
        		ObjectIdentifier oid = opr.getObjectIdentifier();
            	PropertyIdentifier pid = opr.getPropertyIdentifier();

            	BacnetResponse obj = responseMap.get(oid);
            	if(obj == null){
            		obj = new BacnetResponse();
                	
            		obj.setId(remoteDevice.getInstanceNumber()+"_"+oid.getInstanceNumber()+"_"+oid.getObjectType().intValue());
            		obj.setObjectIdentifier(oid.toString());
            	}
            	
            	
            	if(PropertyIdentifier.presentValue.equals(pid)){
            		obj.setValue(value.toString());
        		
            	}else if(PropertyIdentifier.units.equals(pid)){
            		obj.setUnits(value.toString());

            	}else if(PropertyIdentifier.outputUnits.equals(pid)){
            		obj.setOutputUnits(value.toString());
            		
            	}else if(PropertyIdentifier.inactiveText.equals(pid)){
            		obj.setInactiveText(value.toString());
            	
            	}else if(PropertyIdentifier.activeText.equals(pid)){
            		obj.setActiveText(value.toString());
            	}
            	responseMap.put(oid, obj);
        	}
        }
        
        List<BacnetResponse> results = Lists.newArrayList(responseMap.values().iterator());
		return results;
	}
	
	
	/*
	public List<BacnetWriteResponse> execute(BacnetWriteRequest request) throws Exception{
		return null;
	}
	*/
	private class DeviceEventListenerImpl implements DeviceEventListener{
		@Override
		public void iAmReceived(final RemoteDevice d) {
			logger.info("iAmReceived: [instanceNumber="+d.getInstanceNumber()+", address="+d.getAddress().getDescription());
		}
		
		@Override
		public void listenerException(Throwable e) {
			logger.info("listenerException: ");
		}
		@Override
		public boolean allowPropertyWrite(Address from, BACnetObject obj, PropertyValue pv) {
			logger.info("allowPropertyWrite: ");
			return true;
		}
		@Override
		public void propertyWritten(Address from, BACnetObject obj, PropertyValue pv) {
			logger.info("propertyWritten: ");
		}
		@Override
		public void iHaveReceived(RemoteDevice d, RemoteObject o) {
			logger.info("iHaveReceived: ");
		}
		@Override
		public void covNotificationReceived(
				UnsignedInteger subscriberProcessIdentifier,
				RemoteDevice initiatingDevice,
				ObjectIdentifier monitoredObjectIdentifier,
				UnsignedInteger timeRemaining,
				SequenceOf<PropertyValue> listOfValues) {
			logger.info("covNotificationReceived: ");
		}
		@Override
		public void eventNotificationReceived(UnsignedInteger processIdentifier,
				RemoteDevice initiatingDevice,
				ObjectIdentifier eventObjectIdentifier, TimeStamp timeStamp,
				UnsignedInteger notificationClass, UnsignedInteger priority,
				EventType eventType, CharacterString messageText,
				NotifyType notifyType, Boolean ackRequired, EventState fromState,
				EventState toState, NotificationParameters eventValues) {
			logger.info("eventNotificationReceived: ");
		}
		@Override
		public void textMessageReceived(RemoteDevice textMessageSourceDevice,
				Choice messageClass, MessagePriority messagePriority,
				CharacterString message) {
			logger.info("textMessageReceived: ");
		}
		@Override
		public void privateTransferReceived(Address from, UnsignedInteger vendorId,
				UnsignedInteger serviceNumber, Sequence serviceParameters) {
			logger.info("privateTransferReceived: ");
		}
		@Override
		public void reinitializeDevice(Address from,
				ReinitializedStateOfDevice reinitializedStateOfDevice) {
			logger.info("reinitializeDevice: ");
		}
		@Override
		public void synchronizeTime(Address from, DateTime dateTime, boolean utc) {
			logger.info("synchronizeTime: ");
			
		}
	}
	
	
}

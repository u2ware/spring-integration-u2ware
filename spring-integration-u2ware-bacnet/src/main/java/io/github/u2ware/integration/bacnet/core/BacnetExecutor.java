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

	private Log bacnetLogger= LogFactory.getLog(getClass());
	
	protected int localPort = IpNetwork.DEFAULT_PORT; //47808;
	protected int localInstanceNumber;
	
	private LocalDevice localDevice ;
	private Transport transport;
	private IpNetwork network;
	
	public int getLocalPort() {
		return localPort;
	}
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}
	public int getLocalInstanceNumber() {
		return localInstanceNumber;
	}
	public void setLocalInstanceNumber(int localInstanceNumber) {
		this.localInstanceNumber = localInstanceNumber;
	}

	@Override
	public String toString() {
		return "BacnetExecutor [localPort=" + localPort
				+ ", localInstanceNumber=" + localInstanceNumber+"]";
	}
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		
		//int localPort = IpNetwork.DEFAULT_PORT - bacnetDeviceNumber;
		
        network = new IpNetwork(IpNetwork.DEFAULT_BROADCAST_IP, localPort);
        
        transport = new DefaultTransport(network);
//               transport.setTimeout(10000);
//               transport.setSegTimeout(15000);
        
        localInstanceNumber = this.localInstanceNumber > 0 ? this.localInstanceNumber : localPort;
        localDevice = new LocalDevice(localInstanceNumber, transport);
        
		try {
			localDevice.initialize();
	        localDevice.getEventHandler().addListener(new DeviceEventListenerImpl());
	        localDevice.sendGlobalBroadcast(new WhoIsRequest());
	        bacnetLogger.info("BACNet LocalDevice Initialized: <localhost>:"+localPort+"["+localPort+"]");
		
		} catch (Exception e) {
			e.printStackTrace();
	        bacnetLogger.info("BACNet LocalDevice Initialized Error: <localhost>:"+localPort+"["+localPort+"] "+e.getMessage());
			throw new RuntimeException("BACNet LocalDevice Initialized Error: <localhost>:"+localPort+"["+localPort+"]");
		}
	}

	@Override
	public void destroy()  {
		localDevice.terminate();
        bacnetLogger.info("BACNet LocalDevice Terminated: <localhost>:"+localPort+"["+localPort+"]");
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

	public void sendGlobalBroadcast(boolean isNewThread){
		if(isNewThread){
		    super.execute(new Runnable() {
				public void run() {
			        localDevice.sendGlobalBroadcast(new WhoIsRequest());
				}
			});
		}else{
	        localDevice.sendGlobalBroadcast(new WhoIsRequest());
		}
	}

	
	////////////////////////////////////
	//
	////////////////////////////////////
	public List<BacnetResponse> execute(BacnetRequest request) throws Exception{
		if(BacnetRequest.READ_TYPE.equals(request.getType())){
			return readValues(request.getRemoteAddress(), request.getRemoteInstanceNumber());
//
//			if(StringUtils.hasText(request.getRemoteAddress()) && request.getRemoteInstanceNumber() != 0){
//				return readValues(request.getRemoteAddress(), request.getRemoteInstanceNumber());
//			}else{
//				return readValues(getRemoteAddress(), getRemoteInstanceNumber());
//			}
		
		}else{
			return null;
		}
	}

	
	////////////////////////////////////
	//
	////////////////////////////////////
	/*
	public List<BacnetWriteResponse> writeValue(.....) throws Exception{
		return null;
	}
	public List<BacnetResponse> readValues() throws Exception{
		return readValues(getRemoteAddress(), getRemoteInstanceNumber());
	}
	*/
	public List<BacnetResponse> readValues(final String remoteAddress, final int remoteInstanceNumber) throws Exception{		
		Address address = new Address(IpNetworkUtils.toOctetString(remoteAddress));
		RemoteDevice remoteDevice = localDevice.getRemoteDevice(address);
		if(remoteDevice == null){
			remoteDevice = findRemoteDevice(address, remoteInstanceNumber);
	        bacnetLogger.info("BACNet RemoteDevice Find: "+remoteDevice.getAddress().getDescription()+"["+remoteDevice.getInstanceNumber()+"]");
		}
		Collection<ObjectIdentifier> oids = sendReadPropertyAllowNull(remoteDevice);
		PropertyReferences refs = createPropertyReferences(oids);
		PropertyValues pvs = readProperties(remoteDevice, refs);
		
		List<BacnetResponse> result = readValues(pvs, remoteDevice);
        bacnetLogger.info("BACNet RemoteDevice Read: "+remoteDevice.getAddress().getDescription()+"["+remoteDevice.getInstanceNumber()+"]");

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
                	
            		obj.setId(remoteDevice.getInstanceNumber()+"_"+oid.getObjectType().intValue()+"_"+oid.getInstanceNumber());
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
	
	

	///////////////////////
	//
	///////////////////////
	public RemoteDevice findRemoteDevice(final Address address, final int instanceNumber) throws Exception{
		return super.submit(new Callable<RemoteDevice>() {
			@Override
			public RemoteDevice call() throws Exception {
				RemoteDevice d = localDevice.findRemoteDevice(address, instanceNumber);
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
				return pvs;
			}
	    }).get();
	}
	
	
	private class DeviceEventListenerImpl implements DeviceEventListener{
		@Override
		public void iAmReceived(final RemoteDevice d) {
	        bacnetLogger.info("BACNet iAmReceived: "+d);
	        bacnetLogger.info("                  : "+d.getName());
	        bacnetLogger.info("                  : "+d.getModelName());
	        bacnetLogger.info("                  : "+d.getVendorId());
	        bacnetLogger.info("                  : "+d.getVendorName());
	        bacnetLogger.info("                  : "+d.getInstanceNumber());

	        bacnetLogger.info("                  : "+d.getAddress());
	        bacnetLogger.info("                  : "+d.getAddress().getDescription());
	        bacnetLogger.info("                  : "+d.getAddress().getMacAddress());
	        bacnetLogger.info("                  : "+d.getAddress().getMacAddress().getDescription());
	        
	        bacnetLogger.info("                  : "+d.getObjectIdentifier());
	        bacnetLogger.info("                  : "+d.getObjectIdentifier().getInstanceNumber());
	        bacnetLogger.info("                  : "+d.getObjectIdentifier().getObjectType());
		}
		
		@Override
		public void listenerException(Throwable e) {
			bacnetLogger.info("BACNet listenerException: ");
		}
		@Override
		public boolean allowPropertyWrite(Address from, BACnetObject obj, PropertyValue pv) {
			bacnetLogger.info("BACNet allowPropertyWrite: ");
			return true;
		}
		@Override
		public void propertyWritten(Address from, BACnetObject obj, PropertyValue pv) {
			bacnetLogger.info("BACNet propertyWritten: ");
		}
		@Override
		public void iHaveReceived(RemoteDevice d, RemoteObject o) {
			bacnetLogger.info("BACNet iHaveReceived: ");
		}
		@Override
		public void covNotificationReceived(
				UnsignedInteger subscriberProcessIdentifier,
				RemoteDevice initiatingDevice,
				ObjectIdentifier monitoredObjectIdentifier,
				UnsignedInteger timeRemaining,
				SequenceOf<PropertyValue> listOfValues) {
			bacnetLogger.info("BACNet covNotificationReceived: ");
		}
		@Override
		public void eventNotificationReceived(UnsignedInteger processIdentifier,
				RemoteDevice initiatingDevice,
				ObjectIdentifier eventObjectIdentifier, TimeStamp timeStamp,
				UnsignedInteger notificationClass, UnsignedInteger priority,
				EventType eventType, CharacterString messageText,
				NotifyType notifyType, Boolean ackRequired, EventState fromState,
				EventState toState, NotificationParameters eventValues) {
			bacnetLogger.info("BACNet eventNotificationReceived: ");
		}
		@Override
		public void textMessageReceived(RemoteDevice textMessageSourceDevice,
				Choice messageClass, MessagePriority messagePriority,
				CharacterString message) {
			bacnetLogger.info("BACNet textMessageReceived: ");
		}
		@Override
		public void privateTransferReceived(Address from, UnsignedInteger vendorId,
				UnsignedInteger serviceNumber, Sequence serviceParameters) {
			bacnetLogger.info("BACNet privateTransferReceived: ");
		}
		@Override
		public void reinitializeDevice(Address from,
				ReinitializedStateOfDevice reinitializedStateOfDevice) {
			bacnetLogger.info("BACNet reinitializeDevice: ");
		}
		@Override
		public void synchronizeTime(Address from, DateTime dateTime, boolean utc) {
			bacnetLogger.info("BACNet synchronizeTime: ");
		}
	}
}

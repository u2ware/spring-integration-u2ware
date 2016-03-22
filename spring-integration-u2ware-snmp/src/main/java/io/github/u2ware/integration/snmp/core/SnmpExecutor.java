package io.github.u2ware.integration.snmp.core;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibValueSymbol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.asn1.BER;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Lists;

public class SnmpExecutor implements InitializingBean, DisposableBean{

	private Log logger = LogFactory.getLog(getClass());
	
	private ThreadPool threadPool;
	private TransportMapping<?> transport;
	private Snmp snmp;
	private Mib mib ;
	private Map<String,String> mibNames;
	
	private Integer port;
	private String mibFile;
	
	
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getMibFile() {
		return mibFile;
	}
	public void setMibFile(String mibFile) {
		this.mibFile = mibFile;
	}
	@Override
	public void afterPropertiesSet() throws Exception {

		threadPool = ThreadPool.create("Trap", 2);
		MultiThreadedMessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
		
		UdpAddress listenAddress = new UdpAddress(InetAddress.getLocalHost(), port);

	    transport = new DefaultUdpTransportMapping(listenAddress);
	    
	    snmp = new Snmp(dispatcher, transport);
	    snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
	    snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
	    snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());

	    //USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
	    //SecurityModels.getInstance().addSecurityModel(usm);

	    snmp.listen();
	    snmp.addCommandResponder(new CommandResponderImpl());
	    
	    
	    if(mibFile != null){
			File file = new File(mibFile);

			if(file.exists()){

				MibLoader mibLoader = new MibLoader();
				mibLoader.addDir(file.getParentFile());
				
				mib = mibLoader.load(file);
				mibNames = new HashMap<String,String>();
			}
	    }
		logger.info("SNMP Manager Initialized: <localhost>:"+port);		
	}
	
	@Override
	public void destroy() throws Exception{
	    snmp.close();
	    transport.close();
		threadPool.stop();

		logger.info("SNMP Manager Terminated: <localhost>:"+port);		
	}
	
	
	protected class CommandResponderImpl implements CommandResponder{
		@Override
		public void processPdu(CommandResponderEvent event) {
			logger.info("Event: "+event.getPDU());	
		}
	}
	
	////////////////////////////////////
	//
	////////////////////////////////////
	protected Map<String,String> resolveMibNames() {
		return mibNames;
	}
	protected String resolveMibName(String oid) {
		if(mibNames == null){
			return null;
		}
		if(mibNames.containsKey(oid)){
			return mibNames.get(oid);
		}
		MibValueSymbol s = mib.getSymbolByOid(oid);
		String name = s.getName();
		mibNames.put(oid, name);
		return name;
	}
	protected Object resolveValue(Variable v) {
	    switch (v.getSyntax()) {
			case BER.INTEGER: return v.toInt();
		    case BER.BITSTRING: return v.toString();
		    case BER.OCTETSTRING: return v.toString();
		    case BER.OID: return v.toString();
		    case BER.TIMETICKS: return v.toString();
		    case BER.COUNTER: return v.toInt();
		    case BER.COUNTER64: return v.toLong();
		    case BER.ENDOFMIBVIEW: return v.toString();
		    case BER.GAUGE32: return v.toInt();
		    case BER.IPADDRESS: return v.toString();
		    case BER.NOSUCHINSTANCE: return v.toString();
		    case BER.NOSUCHOBJECT: return v.toString();
		    case BER.NULL: return v.toString();
		    case BER.OPAQUE: return v.toString();
	    }
	    return "?";
	}
	protected PDU send(PDU pdu, Target target) throws IOException{
		return snmp.send(pdu, target).getResponse();
	}

	////////////////////////////////////
	//
	////////////////////////////////////
	public Object execute(SnmpRequest snmpRequest) throws Exception {
		return readValues(snmpRequest);
	}

	public synchronized Collection<SnmpResponse> readValues(final SnmpRequest snmpRequest) throws Exception {
		
		final List<SnmpResponse> snmpResponse = Lists.newArrayList();

		VariableBindingResponder listener = new VariableBindingResponder(){
			public void process(VariableBinding vb) {
				SnmpResponse r = convertResponse(snmpRequest, vb);
				snmpResponse.add(r);
			}
		};
		
		Target communityTarget = convertTarget(snmpRequest);
		PDU request = convertRequest(snmpRequest);
		OID rootOID = request.get(0).getOid();
		PDU response = null;

		long startTime = System.currentTimeMillis();
	    do{
			ResponseEvent responseEvent = snmp.send(request, communityTarget);
			response = responseEvent.getResponse();
		}while( ! readValuesProcess(rootOID, request, response, listener));
		
	    logger.info(snmpRequest+", SnmpResponse [size="+snmpResponse.size()
				+", timeInMillis="+ (System.currentTimeMillis()-startTime)
				+"]");
		return snmpResponse;
	}
	
	private boolean readValuesProcess(OID rootOID, PDU request, PDU response, VariableBindingResponder listener) throws IOException {
		if ((response == null) || (response.getErrorStatus() != 0) || (response.getType() == PDU.REPORT)) {
	    	return true;
	    }
        boolean finished = false;
        OID lastOID = request.get(0).getOid();
        for (int i=0; (!finished) && (i<response.size()); i++) {
            VariableBinding vb = response.get(i);

            if ((vb.getOid() == null) ||
            	(vb.getOid().size() < rootOID.size()) ||
                (rootOID.leftMostCompare(rootOID.size(), vb.getOid()) != 0)) {
            	finished = true;
            
            }else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {
            	listener.process(vb);
				finished = true;
            
            }else if (vb.getOid().compareTo(lastOID) <= 0) {
            	throw new IOException("Variable received is not lexicographic successor of requested one:" + vb.toString() + " <= "+lastOID);
            
            }else {
            	listener.process(vb);
				lastOID = vb.getOid();
            }
        }
        if (response.size() == 0) {
        	finished = true;
        }
        if (!finished) {
            VariableBinding next = response.get(response.size()-1);
            next.setVariable(new Null());
            request.set(0, next);
            request.setRequestID(new Integer32(0));
        }
        return finished;
	}

	protected interface VariableBindingResponder {
		public void process(VariableBinding vb);
	}
	
	////////////////////////////////////
	//
	////////////////////////////////////
	private Target convertTarget(SnmpRequest snmpRequest) throws Exception{
	    CommunityTarget communityTarget = new CommunityTarget();
		communityTarget.setCommunity(new OctetString("public"));
		communityTarget.setRetries(3);
		communityTarget.setVersion(SnmpConstants.version1);
		communityTarget.setAddress(new UdpAddress(InetAddress.getByName(snmpRequest.getHost()), snmpRequest.getPort()));
		return communityTarget;
	}
	
	private PDU convertRequest(SnmpRequest snmpRequest) throws Exception {
		OID communityOid = new OID(snmpRequest.getRootOid());
		
	    PDU pdu = new PDU();
	    //pdu.setType(PDU.GETNEXT);
	    pdu.setType(PDU.class.getField(snmpRequest.getPduType()).getInt(pdu));
	    pdu.add(new VariableBinding(communityOid));
	    pdu.setNonRepeaters(0);
	    
	    return pdu;
	}

	private SnmpResponse convertResponse(SnmpRequest request, VariableBinding vb){
		String id = vb.getOid().toString();
		Object value = resolveValue(vb.getVariable());
		String name = resolveMibName(vb.getOid().toString());
		SnmpResponse response = new SnmpResponse();
		response.setId(id);
		response.setValue(value);
		response.setName(name);
		return response;
	}
}

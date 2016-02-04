package io.github.u2ware.integration.snmp.core;

import java.io.File;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibSymbol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
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

public class SnmpExecutor implements CommandResponder, InitializingBean, DisposableBean{

	private Log logger = LogFactory.getLog(getClass());
	
	private ThreadPool threadPool;
	private TransportMapping<?> transport;
	private Snmp snmp;
	private Mib mib ;
	private Map<String,String> mibNames;
	
	private Integer localPort;
	private String mibFile;
	
	
	public Integer getLocalPort() {
		return localPort;
	}
	public void setLocalPort(Integer localPort) {
		this.localPort = localPort;
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
		
		UdpAddress listenAddress = new UdpAddress(InetAddress.getLocalHost(), localPort);

	    transport = new DefaultUdpTransportMapping(listenAddress);
	    
	    snmp = new Snmp(dispatcher, transport);
	    snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
	    snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
	    snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());

	    //USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
	    //SecurityModels.getInstance().addSecurityModel(usm);

	    snmp.listen();
	    snmp.addCommandResponder(this);
	    
	    
	    if(mibFile != null){
			File file = new File(mibFile);

			if(file.exists()){

				MibLoader mibLoader = new MibLoader();
				mibLoader.addDir(file.getParentFile());
				
				mib = mibLoader.load(file);
				mibNames = new HashMap<String,String>();
			}
	    }
		logger.info("SNMP Manager Initialized: <localhost>:"+localPort+",  mibFile="+mibFile);		
	}
	
	@Override
	public void destroy() throws Exception{
	    snmp.close();
	    transport.close();
		threadPool.stop();

		logger.info("SNMP Manager Terminated: <localhost>:"+localPort);		
	}
	
	@Override
	public void processPdu(CommandResponderEvent event) {
		logger.info("Event: "+event);		
	}

	////////////////////////////////////
	//
	////////////////////////////////////
	public Object execute(SnmpRequest snmpRequest) throws Exception {
		return readValue(snmpRequest);
	}
	
	////////////////////////////////////
	//
	////////////////////////////////////
	public synchronized Collection<SnmpResponse> readValue(SnmpRequest snmpRequest) throws Exception {

		List<SnmpResponse> result = Lists.newArrayList();
		
	    CommunityTarget communityTarget = new CommunityTarget();
		communityTarget.setCommunity(new OctetString("public"));
		communityTarget.setRetries(3);
		communityTarget.setVersion(SnmpConstants.version1);
		communityTarget.setAddress(new UdpAddress(InetAddress.getByName(snmpRequest.getHost()), snmpRequest.getPort()));

		
		OID communityOid = new OID(snmpRequest.getRootOid());
		
	    PDU request=new PDU();
	    request.setType(PDU.GETNEXT);
	    request.add(new VariableBinding(communityOid));
	    request.setNonRepeaters(0);

	    OID rootOID = request.get(0).getOid();

	    
	    PDU response = null;
	    int objects = 0;
	    int requests = 0;
	    long startTime = System.currentTimeMillis();

	    
	    do {
	        requests++;
	        ResponseEvent responseEvent = snmp.send(request, communityTarget);
	        response = responseEvent.getResponse();

	        if (response != null) {
	          objects += response.size();
	        }

	    }while (!readValueProcess(snmpRequest, result, response, request, rootOID));
	    
	    logger.info(snmpRequest+", SnmpResponse [size="+result.size()
								+", timeInMillis="+ (System.currentTimeMillis()-startTime)
	    						+", objects="+objects
								+", requests="+requests
								+"]");

	    return result;
	}
	
	private synchronized boolean readValueProcess(SnmpRequest snmpRequest, List<SnmpResponse> snmpResponse, PDU response, PDU request, OID rootOID) throws Exception {

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
            	readValueProcess(snmpRequest, snmpResponse, vb);
				finished = true;
            
            }else if (vb.getOid().compareTo(lastOID) <= 0) {
            	throw new Exception("Variable received is not lexicographic successor of requested one:" + vb.toString() + " <= "+lastOID);
            
            }else {
            	readValueProcess(snmpRequest, snmpResponse, vb);
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
	
	private synchronized void readValueProcess(SnmpRequest req, List<SnmpResponse> res, VariableBinding vb) throws Exception {
		
		String id = vb.getOid().toString();
		Object value = getResponseValue(req, vb.getVariable());
		String name = getResponseName(req, vb.getOid());

		SnmpResponse e = new SnmpResponse();
		e.setId(id);
		e.setValue(value);
		e.setName(name);
		
		res.add(e);
	}
	
	private String getResponseName(SnmpRequest snmpRequest, OID oid) {
		
		if(mibNames == null){
			return null;
		}
		
		String oidText = oid.toString();//.replace('.', '_');
		if(mibNames.containsKey(oidText)){
			return mibNames.get(oidText);
		}
		
		StringBuffer name = new StringBuffer();
		String newOid = oid.toString();
		while(true){
			MibSymbol s = mib.getSymbolByOid(newOid);
			name.insert(0, "."+s.getName());

			if(newOid.lastIndexOf(".") > 0){
				newOid = newOid.substring(0, newOid.lastIndexOf("."));
			}
			
			if(snmpRequest.getRootOid().equals(newOid)){
				s = mib.getSymbolByOid(newOid);
				name.insert(0, s.getName());
				break;
			}
		}

		String n = name.toString();
		mibNames.put(oidText, n);
		return n;
	}
	
	
	private Object getResponseValue(SnmpRequest req, Variable v) {
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
}

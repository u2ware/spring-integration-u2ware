package io.github.u2ware.integration.snmp.inbound;

import io.github.u2ware.integration.snmp.core.SnmpManager;
import io.github.u2ware.integration.snmp.core.SnmpRequest;
import io.github.u2ware.integration.snmp.core.SnmpResponse;
import io.github.u2ware.integration.snmp.support.SnmpHeaders;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * @author kslee@u2ware.com
 * @since 1.0.0
 */
public class SnmpMessageSource extends IntegrationObjectSupport 
implements MessageSource<Collection<SnmpResponse>>{

	private SnmpRequestSupport snmpRequestSupport = new SnmpRequestSupport();
	private String[] snmpRequests;
	private final SnmpManager executor;
	
	public SnmpMessageSource(SnmpManager executor){
		Assert.notNull(executor, "bacnetExecutor must not be null.");
		this.executor = executor;
	}

	public void setSnmpRequests(String... snmpRequests) {
		this.snmpRequests = snmpRequests;
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
		snmpRequestSupport.set(snmpRequests);
	}

	@Override
	public String getComponentType() {
		return "snmp:inbound-channel-adapter";
	}
	
	@Override
	public Message<Collection<SnmpResponse>> receive() {
		
		try{
			SnmpRequest request = snmpRequestSupport.next();
			if(request == null) return null;
			
			Collection<SnmpResponse> response = executor.execute(request);
			if (response == null) {
				return null;
			}

			Map<String, Object> headers = Maps.newHashMap();
			headers.put(SnmpHeaders.REQUEST, request.toString());

			return MessageBuilder.withPayload(response).copyHeaders(headers).build();
			
		}catch(Exception e){
			if(logger.isDebugEnabled())
				logger.debug("SnmpMessageSource Error", e);
			return null;
		}
	}
	
	private static class SnmpRequestSupport{

		private List<SnmpRequest> requests = Lists.newArrayList();
		private int nextIndex = 0;
		
		private void set(String... values) throws Exception{
			for(String value : values){
				requests.add(new SnmpRequest(value));
			}
		}

		private SnmpRequest next(){
			if(requests.size() == 1){
				return requests.get(0);
			}
			
			SnmpRequest result = requests.get(nextIndex);
			nextIndex++;
			if(requests.size() == nextIndex){
				nextIndex = 0;
			}
			return result;
		}
	}
}

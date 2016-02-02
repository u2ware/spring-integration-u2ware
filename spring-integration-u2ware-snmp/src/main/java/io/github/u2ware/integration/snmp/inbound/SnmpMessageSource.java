package io.github.u2ware.integration.snmp.inbound;

import io.github.u2ware.integration.snmp.core.SnmpManager;
import io.github.u2ware.integration.snmp.core.SnmpRequest;
import io.github.u2ware.integration.snmp.core.SnmpResponse;
import io.github.u2ware.integration.snmp.support.SnmpHeaders;

import java.util.Collection;
import java.util.Map;

import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

/**
 * 
 * @author kslee@u2ware.com
 * @since 1.0.0
 */
public class SnmpMessageSource extends IntegrationObjectSupport 
implements MessageSource<Collection<SnmpResponse>>{

	private SnmpRequestSupport requestSupport;
	private final SnmpManager executor;
	
	public SnmpMessageSource(SnmpManager executor){
		Assert.notNull(executor, "bacnetExecutor must not be null.");
		this.executor = executor;
	}

	public void setRequestSupport(SnmpRequestSupport requestSupport) {
		this.requestSupport = requestSupport;
	}

	@Override
	public String getComponentType() {
		return "snmp:inbound-channel-adapter";
	}
	
	@Override
	public Message<Collection<SnmpResponse>> receive() {
		
		try{
			SnmpRequest request = requestSupport.next();
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
}

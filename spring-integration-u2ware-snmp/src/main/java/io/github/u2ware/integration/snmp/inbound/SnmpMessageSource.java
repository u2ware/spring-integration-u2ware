package io.github.u2ware.integration.snmp.inbound;

import io.github.u2ware.integration.snmp.core.SnmpExecutor;
import io.github.u2ware.integration.snmp.core.SnmpRequest;
import io.github.u2ware.integration.snmp.support.SnmpHeaders;

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
implements MessageSource<Object>{

	private final SnmpExecutor executor;
	private SnmpRequestSupport requestSupport;
	
	/**
	 * Constructor taking a {@link SnmpExecutor} that provide all required SNMP
	 * functionality.
	 *
	 * @param executor Must not be null.
	 */
	public SnmpMessageSource(SnmpExecutor executor){
		Assert.notNull(executor, "snmpExecutor must not be null.");
		this.executor = executor;
	}

	public void setRequestSupport(SnmpRequestSupport requestSupport) {
		this.requestSupport = requestSupport;
	}

	@Override
	public String getComponentType() {
		return "snmp:inbound-channel-adapter";
	}
	
	/**
	 * Uses {@link SnmpExecutor#execute()} to executes the SMMP operation.
	 *
	 * If {@link SnmpExecutor#execute()} returns null, this method will return
	 * <code>null</code>. Otherwise, a new {@link Message} is constructed and returned.
	 */
	@Override
	public Message<Object> receive() {
		
		try{
			SnmpRequest request = requestSupport.next();
			if(request == null) return null;
			
			Object response = executor.execute(request);
			if(response == null) return null;

			Map<String, Object> headers = Maps.newHashMap();
			headers.put(SnmpHeaders.REQUEST, request.toString());
			headers.put(SnmpHeaders.LOCAL_PORT, executor.getLocalPort());
			headers.put(SnmpHeaders.MIB_FILE, executor.getMibFile());
			
			return MessageBuilder.withPayload(response).copyHeaders(headers).build();
			
		}catch(Exception e){
			if(logger.isDebugEnabled())
				logger.debug("SnmpMessageSource Error", e);
			return null;
		}
	}	
}

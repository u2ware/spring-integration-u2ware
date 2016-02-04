package io.github.u2ware.integration.snmp.outbound;

import io.github.u2ware.integration.snmp.core.SnmpExecutor;
import io.github.u2ware.integration.snmp.core.SnmpRequest;
import io.github.u2ware.integration.snmp.support.SnmpHeaders;

import java.util.Map;

import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;


/**
 * 
 * @author kslee@u2ware.com
 * @since 1.0.0
 */
public class SnmpMessageHandler extends AbstractReplyProducingMessageHandler {

	private final SnmpExecutor executor;
	private boolean producesReply = true;	//false for outbound-channel-adapter, true for outbound-gateway

	/**
	 * Constructor taking an {@link SnmpExecutor} that wraps common
	 * Bacnet Operations.
	 *
	 * @param executor Must not be null
	 *
	 */
	public SnmpMessageHandler(SnmpExecutor executor) {
		Assert.notNull(executor, "executor must not be null.");
		this.executor = executor;
	}

	/**
	 * If set to 'false', this component will act as an Outbound Channel Adapter.
	 * If not explicitly set this property will default to 'true'.
	 *
	 * @param producesReply Defaults to 'true'.
	 *
	 */
	public void setProducesReply(boolean producesReply) {
		this.producesReply = producesReply;
	}

	/*
	@Override
	protected boolean shouldCopyRequestHeaders() {
		return false;
	}
	*/
	
	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {

		try{
			Object requestPayload = requestMessage.getPayload();
			if(! (requestPayload instanceof SnmpRequest)) {
				return null;
			}
			
			SnmpRequest request = (SnmpRequest)requestPayload;
			Object response = executor.execute(request);
			if (response == null) {
				return null;
			}

			if (producesReply) {
				Map<String, Object> headers = Maps.newHashMap();
				headers.put(SnmpHeaders.REQUEST, request.toString());
				headers.put(SnmpHeaders.LOCAL_PORT, executor.getLocalPort());
				headers.put(SnmpHeaders.MIB_FILE, executor.getMibFile());
				
				return MessageBuilder.withPayload(response).copyHeaders(headers).build();
			}else{ 
				return null;
			}

		}catch(Exception e){
			if(logger.isDebugEnabled())
				logger.debug("SnmpMessageHandler Error", e);
			return null;
		}
	}
}
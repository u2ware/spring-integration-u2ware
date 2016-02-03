package io.github.u2ware.integration.modbus.outbound;

import io.github.u2ware.integration.modbus.core.ModbusExecutor;
import io.github.u2ware.integration.modbus.core.ModbusRequest;
import io.github.u2ware.integration.modbus.support.ModbusHeaders;

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
public class ModbusMessageHandler extends AbstractReplyProducingMessageHandler {

	private final ModbusExecutor executor;
	private boolean producesReply = true;	//false for outbound-channel-adapter, true for outbound-gateway

	/**
	 * Constructor taking an {@link ModbusExecutor} that wraps common
	 * Modbus Operations.
	 *
	 * @param executor Must not be null
	 *
	 */
	public ModbusMessageHandler(ModbusExecutor executor) {
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
			if(! (requestPayload instanceof ModbusRequest)) {
				return null;
			}

			ModbusRequest request = (ModbusRequest)requestPayload;
			Object response = executor.readValues(request);
			if (response == null) {
				return null;
			}

			if (producesReply) {
				Map<String, Object> headers = Maps.newHashMap();
				headers.put(ModbusHeaders.REQUEST, request.toString());
				headers.put(ModbusHeaders.HOST, executor.getHost());
				headers.put(ModbusHeaders.PORT, executor.getPort());

				return MessageBuilder.withPayload(response).copyHeaders(headers).build();
			}else{
				return null;
			}

		}catch(Exception e){
			if(logger.isDebugEnabled())
				logger.debug("ModbusMessageHandler Error", e);
			return null;
		}
	}
}

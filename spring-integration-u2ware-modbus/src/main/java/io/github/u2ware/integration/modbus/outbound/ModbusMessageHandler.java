package io.github.u2ware.integration.modbus.outbound;

import io.github.u2ware.integration.modbus.core.ModbusExecutor;
import io.github.u2ware.integration.modbus.core.ModbusRequest;
import io.github.u2ware.integration.modbus.core.ModbusResponse;
import io.github.u2ware.integration.modbus.support.ModbusHeaders;

import java.util.List;
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
	 * Constructor taking an {@link AbstractModbusExecutor} that wraps common
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

	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {

		try{
			logger.debug("handleRequestMessage: for controller ");

			Object requestPayload = requestMessage.getPayload();

			if(! (requestPayload instanceof ModbusRequest)) {
				return null;
			}

			ModbusRequest bacnetRequest = (ModbusRequest)requestPayload;
			List<ModbusResponse> response = executor.readValues(bacnetRequest);
			if (response == null) {
				return null;
			}

			if (producesReply) {
				Map<String, Object> headers = Maps.newHashMap();
				headers.put(ModbusHeaders.HOST_ADDRESS, executor.getHost());
				headers.put(ModbusHeaders.HOST_PORT, executor.getPort());
				return MessageBuilder.withPayload(response).copyHeaders(headers).build();
			}else{
				return null;
			}

		}catch(Exception e){
			logger.debug("handleRequestMessage", e);
			return null;
		}
	}
}

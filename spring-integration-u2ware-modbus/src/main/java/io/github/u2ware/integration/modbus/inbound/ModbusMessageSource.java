package io.github.u2ware.integration.modbus.inbound;

import io.github.u2ware.integration.modbus.core.ModbusExecutor;
import io.github.u2ware.integration.modbus.core.ModbusRequest;
import io.github.u2ware.integration.modbus.support.ModbusHeaders;

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
public class ModbusMessageSource extends IntegrationObjectSupport 
implements MessageSource<Object>{

	private final ModbusExecutor executor;	
	private ModbusRequestSupport requestSupport;

	
	/**
	 * Constructor taking a {@link AbstractModbusExecutor} that provide all required Modbus
	 * functionality.
	 *
	 * @param executor Must not be null.
	 */
	public ModbusMessageSource(ModbusExecutor executor){
		Assert.notNull(executor, "ModbusExecutor must not be null.");
		this.executor = executor;
	}
	
	public void setRequestSupport(ModbusRequestSupport requestSupport) {
		this.requestSupport = requestSupport;
	}

	
	@Override
	public String getComponentType() {
		return "modbus:inbound-channel-adapter";
	}

	/**
	 * Uses {@link ModbusExecutor#execute()} to executes the Modbus operation.
	 *
	 * If {@link ModbusExecutor#execute()} returns null, this method will return
	 * <code>null</code>. Otherwise, a new {@link Message} is constructed and returned.
	 */
	@Override
	public Message<Object> receive() {
		
		try{
			ModbusRequest request = requestSupport.next();
			if(request == null) return null;
			
			Object response = executor.execute(request);
			if(response == null) return null;
			
			Map<String, Object> headers = Maps.newHashMap();
			headers.put(ModbusHeaders.REQUEST, request.toString());
			headers.put(ModbusHeaders.HOST, executor.getHost());
			headers.put(ModbusHeaders.PORT, executor.getPort());

			return MessageBuilder.withPayload(response).copyHeaders(headers).build();

		}catch(Exception e){
			if(logger.isDebugEnabled())
				logger.debug("ModbusMessageSource Error", e);
			return null;
		}
	}
}

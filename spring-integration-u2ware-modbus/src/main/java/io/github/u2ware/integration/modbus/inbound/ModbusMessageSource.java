package io.github.u2ware.integration.modbus.inbound;

import io.github.u2ware.integration.modbus.core.ModbusExecutor;
import io.github.u2ware.integration.modbus.core.ModbusResponse;
import io.github.u2ware.integration.modbus.support.ModbusHeaders;

import java.util.List;
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
implements MessageSource<List<ModbusResponse>>{

	private final ModbusExecutor executor;	

	private int unitId = 0;
	private int functionCode = 1;
	private int offset = 0;
	private int count = 1;
	
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
	
	@Override
	protected void onInit() throws Exception {
		super.onInit();
	}

	@Override
	public String getComponentType() {
		return "modbus:inbound-channel-adapter";
	}

	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}
	public void setFunctionCode(int functionCode) {
		this.functionCode = functionCode;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public void setCount(int count) {
		this.count = count;
	}


	/**
	 * Uses {@link AbstractModbusExecutor#poll()} to executes the Modbus operation.
	 *
	 * If {@link AbstractModbusExecutor#poll()} returns null, this method will return
	 * <code>null</code>. Otherwise, a new {@link Message} is constructed and returned.
	 */
	@Override
	public Message<List<ModbusResponse>> receive() {
		
		try{
			List<ModbusResponse> response = executor.readValues(unitId, functionCode, offset, count);
			
			if (response == null) {
				return null;
			}
			Map<String, Object> headers = Maps.newHashMap();
			headers.put(ModbusHeaders.HOST_ADDRESS, executor.getHost());
			headers.put(ModbusHeaders.HOST_PORT, executor.getPort());

			return MessageBuilder.withPayload(response).copyHeaders(headers).build();

		}catch(Exception e){
			if(logger.isDebugEnabled())
				logger.debug("Modbus Client Error", e);
			return null;
		}
	}
}

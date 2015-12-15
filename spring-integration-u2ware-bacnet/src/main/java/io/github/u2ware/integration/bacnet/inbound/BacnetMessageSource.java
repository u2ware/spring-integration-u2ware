package io.github.u2ware.integration.bacnet.inbound;

import io.github.u2ware.integration.bacnet.core.BacnetExecutor;
import io.github.u2ware.integration.bacnet.core.BacnetResponse;
import io.github.u2ware.integration.bacnet.support.BacnetHeaders;

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
public class BacnetMessageSource extends IntegrationObjectSupport 
implements MessageSource<List<BacnetResponse>>{

	private final BacnetExecutor executor;

	protected String remoteAddress;
	protected int remoteInstanceNumber;
	
	public String getRemoteAddress() {
		return remoteAddress;
	}
	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	public int getRemoteInstanceNumber() {
		return remoteInstanceNumber;
	}
	public void setRemoteInstanceNumber(int remoteInstanceNumber) {
		this.remoteInstanceNumber = remoteInstanceNumber;
	}
	
	
	/**
	 * Constructor taking a {@link BacnetExecutor} that provide all required Bacnet
	 * functionality.
	 *
	 * @param executor Must not be null.
	 */
	public BacnetMessageSource(BacnetExecutor executor){
		Assert.notNull(executor, "bacnetExecutor must not be null.");
		this.executor = executor;
	}
	
	@Override
	protected void onInit() throws Exception {
		 super.onInit();
	}

	@Override
	public String getComponentType() {
		return "bacnet:inbound-channel-adapter";
	}
	
	/**
	 * Uses {@link BacnetExecutor#poll()} to executes the Bacnet operation.
	 *
	 * If {@link BacnetExecutor#poll()} returns null, this method will return
	 * <code>null</code>. Otherwise, a new {@link Message} is constructed and returned.
	 */
	@Override
	public Message<List<BacnetResponse>> receive() {
		
		try{
			List<BacnetResponse> response = executor.readValues(remoteAddress, remoteInstanceNumber);
			if (response == null) {
				return null;
			}

			Map<String, Object> headers = Maps.newHashMap();
			headers.put(BacnetHeaders.REMOTE_ADDRESS, remoteAddress);
			headers.put(BacnetHeaders.REMOTE_INSTANCE_NUMBER, remoteInstanceNumber);

			return MessageBuilder.withPayload(response).copyHeaders(headers).build();
			
		}catch(Exception e){
			logger.info("BACNet LocalDevice Error", e);
			return null;
		}
	}	
}

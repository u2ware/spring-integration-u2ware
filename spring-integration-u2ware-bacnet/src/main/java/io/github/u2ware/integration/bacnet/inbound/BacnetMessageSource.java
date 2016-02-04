package io.github.u2ware.integration.bacnet.inbound;

import io.github.u2ware.integration.bacnet.core.BacnetExecutor;
import io.github.u2ware.integration.bacnet.core.BacnetRequest;
import io.github.u2ware.integration.bacnet.support.BacnetHeaders;

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
implements MessageSource<Object>{

	private final BacnetExecutor executor;
	private BacnetRequestSupport requestSupport;
	
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

	public void setRequestSupport(BacnetRequestSupport requestSupport) {
		this.requestSupport = requestSupport;
	}

	@Override
	public String getComponentType() {
		return "bacnet:inbound-channel-adapter";
	}
	
	/**
	 * Uses {@link BacnetExecutor#execute()} to executes the Bacnet operation.
	 *
	 * If {@link BacnetExecutor#execute()} returns null, this method will return
	 * <code>null</code>. Otherwise, a new {@link Message} is constructed and returned.
	 */
	@Override
	public Message<Object> receive() {

		try{
			BacnetRequest request = requestSupport.next();
			if(request == null) return null;
			
			Object response = executor.execute(request);
			if(response == null) return null;

			Map<String, Object> headers = Maps.newHashMap();
			headers.put(BacnetHeaders.REQUEST, request.toString());
			headers.put(BacnetHeaders.PORT, executor.getPort());

			return MessageBuilder.withPayload(response).copyHeaders(headers).build();
			
		}catch(Exception e){
			if(logger.isDebugEnabled())
				logger.debug("BacnetMessageSource Error", e);
			return null;
		}
	}
}

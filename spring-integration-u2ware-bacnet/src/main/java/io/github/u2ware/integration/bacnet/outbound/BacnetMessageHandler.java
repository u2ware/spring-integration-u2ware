package io.github.u2ware.integration.bacnet.outbound;

import io.github.u2ware.integration.bacnet.core.BacnetExecutor;
import io.github.u2ware.integration.bacnet.core.BacnetRequest;
import io.github.u2ware.integration.bacnet.core.BacnetResponse;
import io.github.u2ware.integration.bacnet.support.BacnetHeaders;

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
public class BacnetMessageHandler extends AbstractReplyProducingMessageHandler {

	private final BacnetExecutor executor;
	private boolean producesReply = true;	//false for outbound-channel-adapter, true for outbound-gateway

	/**
	 * Constructor taking an {@link BacnetExecutor} that wraps common
	 * Bacnet Operations.
	 *
	 * @param executor Must not be null
	 *
	 */
	public BacnetMessageHandler(BacnetExecutor executor) {
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

			if(! (requestPayload instanceof BacnetRequest)) {
				return null;
			}
			
			BacnetRequest bacnetRequest = (BacnetRequest)requestPayload;
			List<BacnetResponse> response = executor.execute(bacnetRequest);
			if (response == null) {
				return null;
			}

			if (producesReply) {
				Map<String, Object> headers = Maps.newHashMap();
				headers.put(BacnetHeaders.REMOTE_ADDRESS, executor.getRemoteAddress());
				headers.put(BacnetHeaders.REMOTE_INSTANCE_NUMBER, executor.getRemoteInstanceNumber());
				return MessageBuilder.withPayload(response).copyHeaders(headers).build();
			}else{
				return null;
			}

		}catch(Exception e){
			logger.info("BACNet LocalDevice Error", e);
			return null;
		}
	}
}
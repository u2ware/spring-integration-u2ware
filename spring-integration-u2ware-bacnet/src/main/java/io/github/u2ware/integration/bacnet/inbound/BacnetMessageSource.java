package io.github.u2ware.integration.bacnet.inbound;

import io.github.u2ware.integration.bacnet.core.BacnetExecutor;
import io.github.u2ware.integration.bacnet.core.BacnetRequest;
import io.github.u2ware.integration.bacnet.core.BacnetResponse;
import io.github.u2ware.integration.bacnet.support.BacnetHeaders;

import java.util.List;
import java.util.Map;

import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * @author kslee@u2ware.com
 * @since 1.0.0
 */
public class BacnetMessageSource extends IntegrationObjectSupport 
implements MessageSource<List<BacnetResponse>>{

	private final BacnetExecutor executor;

	private BacnetRequestSupport bacnetRequestSupport = new BacnetRequestSupport();

	public void setRemoteAddress(String remoteAddress) {
		bacnetRequestSupport.setRemoteAddress(remoteAddress);
	}
	public void setRemoteInstanceNumber(int remoteInstanceNumber) {
		bacnetRequestSupport.setRemoteInstanceNumber(remoteInstanceNumber);
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
		 bacnetRequestSupport.init();
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
			BacnetRequest bacnetRequest = bacnetRequestSupport.next();
			
			List<BacnetResponse> bacnetResponse = executor.execute(bacnetRequest);
			if (bacnetResponse == null) {
				return null;
			}

			Map<String, Object> headers = Maps.newHashMap();
			headers.put(BacnetHeaders.REMOTE_ADDRESS, bacnetRequest.getRemoteAddress());
			headers.put(BacnetHeaders.REMOTE_INSTANCE_NUMBER, bacnetRequest.getRemoteInstanceNumber());

			return MessageBuilder.withPayload(bacnetResponse).copyHeaders(headers).build();
			
		}catch(Exception e){
			if(logger.isDebugEnabled())
				logger.debug("BACNet LocalDevice Error", e);
			return null;
		}
	}
	
	private static class BacnetRequestSupport{

		private String remoteAddress;
		private int remoteInstanceNumber;

		private List<BacnetRequest> requests = Lists.newArrayList();
		private int nextIndex = -1;
		
		public void setRemoteAddress(String remoteAddress) {
			this.remoteAddress = remoteAddress;
		}
		public void setRemoteInstanceNumber(int remoteInstanceNumber) {
			this.remoteInstanceNumber = remoteInstanceNumber;
		}
		
		public void init() {
			
			String[] itemArrays = StringUtils.commaDelimitedListToStringArray(remoteAddress);
			if(itemArrays == null || itemArrays.length == 1){
				nextIndex = -1;
				requests.add(new BacnetRequest(remoteAddress, remoteInstanceNumber));

			}else{
				nextIndex = 0;
				for(String itemArray : itemArrays){
					
					String[] item = StringUtils.delimitedListToStringArray(itemArray, "_");
					String address = item[0];
					int instanceNumber = Integer.parseInt(item[1]);
					requests.add(new BacnetRequest(address, instanceNumber));
				}
			}
		}
		
		private BacnetRequest next(){
			if(requests.size() == 1){
				return requests.get(0);
			}
			
			BacnetRequest result = requests.get(nextIndex);
			nextIndex++;
			if(requests.size() == nextIndex){
				nextIndex = 0;
			}
			return result;
		}
	}
}

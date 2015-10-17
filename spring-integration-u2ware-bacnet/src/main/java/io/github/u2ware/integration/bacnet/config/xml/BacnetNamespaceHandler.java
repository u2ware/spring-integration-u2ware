package io.github.u2ware.integration.bacnet.config.xml;

import org.springframework.integration.config.xml.AbstractIntegrationNamespaceHandler;

/**
 * The namespace handler for the Bacnet namespace
 *
 * @author kslee@u2ware.com
 * @since 1.0.0
 *
 */
public class BacnetNamespaceHandler extends AbstractIntegrationNamespaceHandler {

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
	 */
	public void init() {
		
		this.registerBeanDefinitionParser("inbound-channel-adapter",  new BacnetInboundChannelAdapterParser());
		this.registerBeanDefinitionParser("outbound-channel-adapter", new BacnetOutboundChannelAdapterParser());
		this.registerBeanDefinitionParser("outbound-gateway", new BacnetOutboundGatewayParser());
		
	}
}

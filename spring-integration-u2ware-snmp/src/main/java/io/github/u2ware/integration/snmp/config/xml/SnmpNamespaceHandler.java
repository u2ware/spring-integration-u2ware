package io.github.u2ware.integration.snmp.config.xml;

import org.springframework.integration.config.xml.AbstractIntegrationNamespaceHandler;

/**
 * The namespace handler for the SNMP namespace
 *
 * @author kslee@u2ware.com
 * @since 1.0.0
 *
 */
public class SnmpNamespaceHandler extends AbstractIntegrationNamespaceHandler {

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
	 */
	public void init() {
		
		this.registerBeanDefinitionParser("inbound-channel-adapter",  new SnmpInboundChannelAdapterParser());
		this.registerBeanDefinitionParser("outbound-channel-adapter", new SnmpOutboundChannelAdapterParser());
		this.registerBeanDefinitionParser("outbound-gateway", new SnmpOutboundGatewayParser());
		
	}
}

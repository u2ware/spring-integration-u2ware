package io.github.u2ware.integration.snmp.config.xml;

import io.github.u2ware.integration.snmp.core.SnmpManager;
import io.github.u2ware.integration.snmp.outbound.SnmpMessageHandler;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractConsumerEndpointParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;



/**
 * The parser for SNMP outbound gateway.
 *
 * @author kslee@u2ware.com
 * @since 1.0.0
 */
public class SnmpOutboundGatewayParser extends AbstractConsumerEndpointParser  {


	@Override
	protected String getInputChannelAttributeName() {
		return "request-channel";
	}
	
	@Override
	protected BeanDefinitionBuilder parseHandler(Element element, ParserContext parserContext) {

		final BeanDefinitionBuilder outboundGatewayBuilder = BeanDefinitionBuilder.genericBeanDefinition(SnmpMessageHandler.class);

		String channelAdapterId = this.resolveId(element, outboundGatewayBuilder.getRawBeanDefinition(), parserContext);

		////////
		final BeanDefinitionBuilder executorBuilder = BeanDefinitionBuilder.genericBeanDefinition(SnmpManager.class);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "localPort");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "LocalMib");

		final BeanDefinition executorBuilderBeanDefinition = executorBuilder.getBeanDefinition();
		final String executorBeanName = channelAdapterId + ".snmpManager";
		parserContext.registerBeanComponent(new BeanComponentDefinition(executorBuilderBeanDefinition, executorBeanName));
		////////
		
		
		outboundGatewayBuilder.addConstructorArgReference(executorBeanName);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(outboundGatewayBuilder, element, "reply-timeout", "sendTimeout");
		final String replyChannel = element.getAttribute("reply-channel");
		if (StringUtils.hasText(replyChannel)) {
			outboundGatewayBuilder.addPropertyReference("outputChannel", replyChannel);
		}

		return outboundGatewayBuilder;

	}
}

package io.github.u2ware.integration.bacnet.config.xml;

import io.github.u2ware.integration.bacnet.core.BacnetExecutor;
import io.github.u2ware.integration.bacnet.outbound.BacnetMessageHandler;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractConsumerEndpointParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;



/**
 * The parser for Bacnet outbound gateway.
 *
 * @author kslee@u2ware.com
 * @since 1.0.0
 */
public class BacnetOutboundGatewayParser extends AbstractConsumerEndpointParser  {


	@Override
	protected String getInputChannelAttributeName() {
		return "request-channel";
	}
	
	@Override
	protected BeanDefinitionBuilder parseHandler(Element element, ParserContext parserContext) {


		final BeanDefinitionBuilder outboundGatewayBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(BacnetMessageHandler.class);
		String channelAdapterId = this.resolveId(element, outboundGatewayBuilder.getRawBeanDefinition(), parserContext);

		////////
		final BeanDefinitionBuilder executorBuilder = BeanDefinitionBuilder.genericBeanDefinition(BacnetExecutor.class);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "local-port", "localPort");

		final BeanDefinition executorBuilderBeanDefinition = executorBuilder.getBeanDefinition();
		final String executorBeanName = channelAdapterId + ".bacnetExecutor";
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

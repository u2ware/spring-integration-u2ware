package io.github.u2ware.integration.modbus.config.xml;

import io.github.u2ware.integration.modbus.core.ModbusExecutor;
import io.github.u2ware.integration.modbus.outbound.ModbusMessageHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractConsumerEndpointParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;



/**
 * The parser for Modbus outbound gateway.
 *
 * @author kslee@u2ware.com
 * @since 1.0.0
 */
public class ModbusOutboundGatewayParser extends AbstractConsumerEndpointParser  {

	private Log logger = LogFactory.getLog(getClass());

	@Override
	protected String getInputChannelAttributeName() {
		return "request-channel";
	}

	@Override
	protected BeanDefinitionBuilder parseHandler(Element element, ParserContext parserContext) {

		logger.debug("OutboundGatewayParser");

		final BeanDefinitionBuilder outboundGatewayBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(ModbusMessageHandler.class);
		String channelAdapterId = this.resolveId(element, outboundGatewayBuilder.getRawBeanDefinition(), parserContext);

		////////
		final BeanDefinitionBuilder executorBuilder = BeanDefinitionBuilder.genericBeanDefinition(ModbusExecutor.class);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "componentName");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "host");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "port");

		final BeanDefinition executorBuilderBeanDefinition = executorBuilder.getBeanDefinition();
		final String executorBeanName = channelAdapterId + ".modbusExecutor";
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

package io.github.u2ware.integration.bacnet.config.xml;

import io.github.u2ware.integration.bacnet.core.BacnetExecutor;
import io.github.u2ware.integration.bacnet.outbound.BacnetMessageHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;


/**
 * The parser for the Bacnet outbound channel adapter.
 *
 * @author kslee@u2ware.com
 * @since 1.0.0
 */
public class BacnetOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {

	private Log logger = LogFactory.getLog(getClass());

	@Override
	protected boolean shouldGenerateId() {
		return false;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	@Override
	protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext) {

		logger.debug("OutboundChannelAdapterParser");

		final BeanDefinitionBuilder outboundChannelAdapterBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(BacnetMessageHandler.class);
		String channelAdapterId = this.resolveId(element, outboundChannelAdapterBuilder.getRawBeanDefinition(), parserContext);

		////////
		final BeanDefinitionBuilder executorBuilder = BeanDefinitionBuilder.genericBeanDefinition(BacnetExecutor.class);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "localPort");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "componentName");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "remoteAddress");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "remoteInstanceNumber");

		final BeanDefinition executorBuilderBeanDefinition = executorBuilder.getBeanDefinition();
		final String executorBeanName = channelAdapterId + ".bacnetExecutor";
		parserContext.registerBeanComponent(new BeanComponentDefinition(executorBuilderBeanDefinition, executorBeanName));
		////////
		
		outboundChannelAdapterBuilder.addConstructorArgReference(executorBeanName);
		outboundChannelAdapterBuilder.addPropertyValue("producesReply", Boolean.FALSE);

		return outboundChannelAdapterBuilder.getBeanDefinition();

	}

}

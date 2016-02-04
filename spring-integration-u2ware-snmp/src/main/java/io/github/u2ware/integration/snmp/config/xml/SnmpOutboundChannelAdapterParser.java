package io.github.u2ware.integration.snmp.config.xml;

import io.github.u2ware.integration.snmp.core.SnmpExecutor;
import io.github.u2ware.integration.snmp.outbound.SnmpMessageHandler;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;


/**
 * The parser for the SNMP outbound channel adapter.
 *
 * @author kslee@u2ware.com
 * @since 1.0.0
 */
public class SnmpOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {

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

		final BeanDefinitionBuilder outboundChannelAdapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(SnmpMessageHandler.class);

		String channelAdapterId = this.resolveId(element, outboundChannelAdapterBuilder.getRawBeanDefinition(), parserContext);

		////////
		final BeanDefinitionBuilder executorBuilder = BeanDefinitionBuilder.genericBeanDefinition(SnmpExecutor.class);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "port");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "mib-file", "mibFile");

		final BeanDefinition executorBuilderBeanDefinition = executorBuilder.getBeanDefinition();
		final String executorBeanName = channelAdapterId + ".snmpExecutor";
		parserContext.registerBeanComponent(new BeanComponentDefinition(executorBuilderBeanDefinition, executorBeanName));
		////////
		
		outboundChannelAdapterBuilder.addConstructorArgReference(executorBeanName);
		outboundChannelAdapterBuilder.addPropertyValue("producesReply", Boolean.FALSE);

		return outboundChannelAdapterBuilder.getBeanDefinition();

	}

}

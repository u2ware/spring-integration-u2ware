/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.u2ware.integration.snmp.config.xml;

import io.github.u2ware.integration.snmp.core.SnmpExecutor;
import io.github.u2ware.integration.snmp.inbound.SnmpMessageSource;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;


/**
 * The SNMP inbound channel adapter parser
 *
 * @author kslee@u2ware.com
 * @since 1.0.0
 *
 */
public class SnmpInboundChannelAdapterParser extends AbstractPollingInboundChannelAdapterParser{

	protected BeanMetadataElement parseSource(Element element, ParserContext parserContext) {
		
		BeanDefinitionBuilder pollingChannelAdapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(SnmpMessageSource.class);		
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(pollingChannelAdapterBuilder, element, "request-support", "requestSupport");
		
		String channelAdapterId = this.resolveId(element, pollingChannelAdapterBuilder.getRawBeanDefinition(), parserContext);

		////////
		final BeanDefinitionBuilder executorBuilder = BeanDefinitionBuilder.genericBeanDefinition(SnmpExecutor.class);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "port");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "mib-file", "mibFile");
		

		final BeanDefinition executorBuilderBeanDefinition = executorBuilder.getBeanDefinition();
		final String executorBeanName = channelAdapterId + ".snmpExecutor";
		parserContext.registerBeanComponent(new BeanComponentDefinition(executorBuilderBeanDefinition, executorBeanName));
		////////
		
		pollingChannelAdapterBuilder.addConstructorArgReference(executorBeanName);
		
		return pollingChannelAdapterBuilder.getBeanDefinition();
	}
}

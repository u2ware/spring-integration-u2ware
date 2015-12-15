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
package io.github.u2ware.integration.bacnet.config.xml;

import io.github.u2ware.integration.bacnet.core.BacnetExecutor;
import io.github.u2ware.integration.bacnet.inbound.BacnetMessageSource;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;


/**
 * The Bacnet inbound channel adapter parser
 *
 * @author kslee@u2ware.com
 * @since 1.0.0
 *
 */
public class BacnetInboundChannelAdapterParser extends AbstractPollingInboundChannelAdapterParser{

	protected BeanMetadataElement parseSource(Element element, ParserContext parserContext) {
		
		BeanDefinitionBuilder pollingChannelAdapterBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(BacnetMessageSource.class);		
		String channelAdapterId = this.resolveId(element, pollingChannelAdapterBuilder.getRawBeanDefinition(), parserContext);

		IntegrationNamespaceUtils.setValueIfAttributeDefined(pollingChannelAdapterBuilder, element, "remoteAddress");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(pollingChannelAdapterBuilder, element, "remoteInstanceNumber");

		////////
		final BeanDefinitionBuilder executorBuilder = BeanDefinitionBuilder.genericBeanDefinition(BacnetExecutor.class);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(executorBuilder, element, "localPort");
		

		final BeanDefinition executorBuilderBeanDefinition = executorBuilder.getBeanDefinition();
		final String executorBeanName = channelAdapterId + ".bacnetExecutor";
		parserContext.registerBeanComponent(new BeanComponentDefinition(executorBuilderBeanDefinition, executorBeanName));
		////////
		
		pollingChannelAdapterBuilder.addConstructorArgReference(executorBeanName);
		
		return pollingChannelAdapterBuilder.getBeanDefinition();
	}
}

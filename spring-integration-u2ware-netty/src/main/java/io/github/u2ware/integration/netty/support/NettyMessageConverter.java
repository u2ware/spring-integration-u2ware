package io.github.u2ware.integration.netty.support;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class NettyMessageConverter implements MessageConverter {

	//protected Log logger = LogFactory.getLog(getClass());

	@Override
	public Object fromMessage(Message<?> message, Class<?> targetClass) {
		Assert.notNull(message, "message must not be null.");
		Object payload = message.getPayload();
		if(targetClass != null){
			if(! ClassUtils.isAssignableValue(targetClass, payload)){
				throw new MessageConversionException("Failed to convert netty event to a Message");
			}
		}
		return message.getPayload();
	}

	@Override
	public Message<?> toMessage(Object payload, MessageHeaders header) {
		Assert.notNull(payload, "payload must not be null.");
		MessageBuilder<?> builder = MessageBuilder.withPayload(payload);
		if(header != null){
			builder.copyHeaders(header);
		}
		return  builder.build();
	}
}
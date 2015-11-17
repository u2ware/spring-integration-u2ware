package io.github.u2ware.integration.netty.support;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.http.MediaType;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;
import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

/**
 *
 * @author u2waremanager@gamil.com
 */
public class HttpMessageConverter implements MessageConverter {

	//protected Log logger = LogFactory.getLog(getClass());

	private final MessageBuilderFactory messageBuilderFactory;

	public HttpMessageConverter() {
		this(new DefaultMessageBuilderFactory());
	}

	public HttpMessageConverter(MessageBuilderFactory messageBuilderFactory) {
		this.messageBuilderFactory = messageBuilderFactory;
	}

	@Override
	public Object fromMessage(Message<?> message, Class<?> targetClass) {

		Assert.notNull(message, "message must not be null.");
		Assert.isAssignable(FullHttpResponse.class, targetClass);

		try{
			String payload = message.getPayload().toString();
			ByteBuf content = ByteBufUtil.encodeString(UnpooledByteBufAllocator.DEFAULT, CharBuffer.wrap(payload), Charset.defaultCharset());
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);

			/////////////////////
			//Http Headers
			/////////////////////
			for(String key : message.getHeaders().keySet()){
				if(MessageHeaders.CONTENT_TYPE.equals(key)){
					response.headers().set(HttpHeaders.Names.CONTENT_TYPE, message.getHeaders().get(key));
					//logger.debug("HTTP Response Headers: "+HttpHeaders.Names.CONTENT_TYPE+"="+message.getHeaders().get(key));
				}else{
					response.headers().set(key, message.getHeaders().get(key));			
					//logger.debug("HTTP Response Headers: "+key+"="+message.getHeaders().get(key));
				}
	        }
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
	        
			return response;
			
		}catch(Exception e){
			e.printStackTrace();
			throw new MessageConversionException("Failed to convert netty event to a Message", e);
		}
	}

	@Override
	public Message<?> toMessage(Object payload, MessageHeaders header) {
		
		Assert.isInstanceOf(FullHttpRequest.class, payload);
		
		try {
			FullHttpRequest request = (FullHttpRequest) payload;
			
			
			/////////////////////
			//Message Headers
			//////////////////////
			Map<String, Object> messageHeaders = Maps.newHashMap();

			boolean keepAlive = HttpHeaders.isKeepAlive(request);
			Charset charsetToUse = null;
			boolean binary = false;
			for (Entry<String, String> entry : request.headers()) {
				if (entry.getKey().equalsIgnoreCase("Content-Type")) {
					MediaType contentType = MediaType.parseMediaType(entry.getValue());
					charsetToUse = contentType.getCharSet();
					messageHeaders.put(MessageHeaders.CONTENT_TYPE, entry.getValue());
					binary = MediaType.APPLICATION_OCTET_STREAM.equals(contentType);
				
				}else if (!entry.getKey().toUpperCase().startsWith("ACCEPT")
					&& !entry.getKey().toUpperCase().equals("CONNECTION")) {
					messageHeaders.put(entry.getKey(), entry.getValue());
				}
			}
			messageHeaders.put(NettyHeaders.HTTP_REQUEST_PATH, request.getUri());
			messageHeaders.put(NettyHeaders.HTTP_REQUEST_METHOD, request.getMethod().toString());
			if(keepAlive){
				messageHeaders.put(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			}
			if(header != null){
				messageHeaders.putAll(header);
			}

			/////////////////////
			//Message Payload
			//////////////////////
			ByteBuf buf = request.content();
			AbstractIntegrationMessageBuilder<?> builder;
			if (binary) {
				byte[] content = buf.array();
				//logger.debug("HTTP Request Content: "+content);
				builder = this.messageBuilderFactory.withPayload(content);
			
			}else {
				// ISO-8859-1 is the default http charset when not set
				charsetToUse = charsetToUse == null ? Charset.forName("ISO-8859-1") : charsetToUse;
				String content = buf.toString(charsetToUse);
				
				//logger.debug("HTTP Request Content: "+content);
				builder = this.messageBuilderFactory.withPayload(content);
			}
			builder.copyHeaders(messageHeaders);
			return builder.build();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new MessageConversionException("Failed to convert netty event to a Message", ex);
		}
	}
}
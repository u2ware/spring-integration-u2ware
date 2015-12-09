package io.github.u2ware.integration.netty.x;

import io.github.u2ware.integration.netty.core.AbstractTcpServer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

public class HttpResourceServer extends AbstractTcpServer implements ResourceLoaderAware{

    private String resourceLocation;
	private ResourceLoader resourceLoader;
	
	public void setResourceLocation(String resourceLocation) {
		this.resourceLocation = resourceLocation;
	}
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	protected void initChannelPipeline(ChannelPipeline pipeline) throws Exception {
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpResourceHandler(resourceLoader, resourceLocation));
	}
}

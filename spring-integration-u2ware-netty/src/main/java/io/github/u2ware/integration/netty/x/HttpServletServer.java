package io.github.u2ware.integration.netty.x;

import io.github.u2ware.integration.netty.core.AbstractTcpServer;
import io.github.u2ware.integration.netty.support.NettyLoggingHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class HttpServletServer extends AbstractTcpServer implements EnvironmentAware, ResourceLoaderAware, InitializingBean, DisposableBean{

	private final Log nettyLogger = LogFactory.getLog(getClass());

	private DispatcherServlet dispatcherServlet;
	private Class<?> configClass;
	
	public void setConfigClass(Class<?> configClass) {
		this.configClass = configClass;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		
		
		MockServletContext servletContext = new MockServletContext();
		MockServletConfig servletConfig = new MockServletConfig(servletContext);

		AnnotationConfigWebApplicationContext wac = new AnnotationConfigWebApplicationContext();
		wac.setServletContext(servletContext);
		wac.setClassLoader(getClass().getClassLoader());
		wac.setServletConfig(servletConfig);
		//wac.setEnvironment((ConfigurableEnvironment) environment);
		wac.register(configClass);
		wac.refresh();

		this.dispatcherServlet = new DispatcherServlet(wac);
		this.dispatcherServlet.init(servletConfig);

		
		//set spring config in xml
		//this.dispatcherServlet = new DispatcherServlet();
		//this.dispatcherServlet.setContextConfigLocation("classpath*:/applicationContext.xml");
		//this.dispatcherServlet.init(servletConfig);
	}
	
	@Override
	public void destroy() throws Exception {
		dispatcherServlet.destroy();
		super.destroy();
	}
	
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		
	}
	@Override
	public void setEnvironment(Environment environment) {

	}

	@Override
	protected void initChannelPipeline(ChannelPipeline pipeline) throws Exception {
		pipeline.addLast("logging", new NettyLoggingHandler(nettyLogger, false));
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
		pipeline.addLast("handler", new HttpServletHandler(nettyLogger, this.dispatcherServlet));
	}

}

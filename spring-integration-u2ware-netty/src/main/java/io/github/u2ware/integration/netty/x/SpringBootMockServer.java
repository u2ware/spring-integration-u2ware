package io.github.u2ware.integration.netty.x;

import io.github.u2ware.integration.netty.core.AbstractTcpServer;
import io.github.u2ware.integration.netty.support.NettyLoggingHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.servlet.DispatcherServlet;

public class SpringBootMockServer extends AbstractTcpServer implements EnvironmentAware, ResourceLoaderAware, InitializingBean, DisposableBean{

	private Log logger = LogFactory.getLog(getClass());
	
	private DispatcherServlet dispatcherServlet;
	private Map<String, Object> defaultProperties;
	//private Environment environment;
	private ResourceLoader resourceLoader;
	private Class<?> configClass;
	
	public void setConfigClass(Class<?> configClass) {
		this.configClass = configClass;
	}
	public void setDefaultProperties(Map<String, Object> defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		StandardServletEnvironment env = new StandardServletEnvironment();
		MutablePropertySources sources = env.getPropertySources();
		if (this.defaultProperties != null && !this.defaultProperties.isEmpty()) {
			sources.addLast(new MapPropertySource("defaultProperties", this.defaultProperties));
		}
		Resource resource1 = resourceLoader.getResource("classpath:/application.properties");
		if(resource1.exists()){
			sources.addLast(new ResourcePropertySource("applicationPropties", resource1));
		}else{
			Resource resource2 = resourceLoader.getResource("classpath:/application.yml");
			if(resource2.exists()){
				sources.addLast(new ResourcePropertySource("applicationYml", resource2));
			}
		}
		
		MockServletContext servletContext = new MockServletContext();
		MockServletConfig servletConfig = new MockServletConfig(servletContext);
		
		AnnotationConfigWebApplicationContext wac = new AnnotationConfigWebApplicationContext();
		wac.setServletContext(servletContext);
		//wac.setClassLoader(resourceLoader.getClassLoader());
		wac.setServletConfig(servletConfig);
		wac.setEnvironment(env);
		wac.register(configClass);
		wac.refresh();

		this.dispatcherServlet = new DispatcherServlet(wac);
		this.dispatcherServlet.init(servletConfig);
		
		if(logger.isDebugEnabled()){
			String[] beanNames = wac.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for(String beanName : beanNames){
				Object beanObject = wac.getBean(beanName);
				if(beanObject != null){
					logger.debug(beanName+"="+beanObject.getClass());
				}else{
					logger.debug(beanName+"="+null);
				}
			}
		}
		//set spring config in xml
		//this.dispatcherServlet = new DispatcherServlet();
		//this.dispatcherServlet.setContextConfigLocation("classpath*:/applicationContext.xml");
		//this.dispatcherServlet.init(servletConfig);
		Integer port = 8080;
		try{
			port =  Integer.parseInt(env.getProperty("server.port"));
		}catch(Exception e){
			
		}
		super.setPort(port);
		
		super.afterPropertiesSet();
	}
	
	@Override
	public void destroy() throws Exception {
		dispatcherServlet.destroy();
		super.destroy();
	}
	
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	@Override
	public void setEnvironment(Environment environment) {
		//this.environment = environment;
	}

	@Override
	protected void initChannelPipeline(ChannelPipeline pipeline) throws Exception {
		pipeline.addLast("logging", new NettyLoggingHandler(getClass(), false));
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
		pipeline.addLast("handler", new SpringBootMockServerHandler(getClass(), this.dispatcherServlet));
	}

}

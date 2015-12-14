package io.github.u2ware.integration.netty.test.http.servlet.app.helloworld;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloworldController implements EnvironmentAware{

    protected Log logger = LogFactory.getLog(getClass());

    private Environment environment;
    
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
    
    @RequestMapping(value="/helloworld", produces = "text/html; charset=utf-8")
    public @ResponseBody String world(HttpServletRequest request) {
    	
    	logger.debug("helloworld");
    	logger.debug("helloworld");
    	logger.debug(""+environment);
    	logger.debug(""+environment.getProperty("hello"));
    	logger.debug(""+environment.getProperty("foo"));
    	logger.debug("helloworld");
    	
        return "hello world";
    }

}
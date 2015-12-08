package io.github.u2ware.integration.netty.test.http_webmvc_server.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    protected Log logger = LogFactory.getLog(getClass());

    @RequestMapping(value="/hello")
    public @ResponseBody String foo() {
    	logger.debug("HelloController");
    	logger.debug("HelloController");
        return "hello world";
    }
}
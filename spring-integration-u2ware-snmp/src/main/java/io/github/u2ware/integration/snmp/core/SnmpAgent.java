package io.github.u2ware.integration.snmp.core;

import java.io.File;

import org.snmp4j.agent.example.SampleAgent;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.util.ArgumentParser;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

public class SnmpAgent extends SampleAgent implements InitializingBean, DisposableBean{

	private static SnmpAgent instance;
	
	public static void startup(int port) throws Exception{
		instance = new SnmpAgent(port);
		instance.afterPropertiesSet();
	}
	public static void shutdown() throws Exception{
		instance.destroy();
	}

	/////////////
	//
	/////////////
	private static final String optionFormat = "-c[s{=SampleAgent.cfg}] -bc[s{=SampleAgent.bc}] "+ "+ts[s] +cfg[s] +tls-version[s{=TLSv1}<TLSv1[\\.1|\\.2]?[,TLSv1[\\.1|\\.2]?]*>] ";
	private static final String parameterFormat = "#address[s<(udp|tcp|tls):.*[/[0-9]+]?>] ..";
	private static final String argsFormat = "-c recycle/SampleAgent.cfg -bc recycle/SampleAgent.bc -tls-version TLSv1 udp:127.0.0.1/%d tcp:127.0.0.1/%d";
	static {
	    org.snmp4j.log.LogFactory.setLogFactory(null);
	}

	private int port;
	
	private SnmpAgent(int port) throws Exception {
		super(
			new ArgumentParser(optionFormat, parameterFormat)
				.parse(StringUtils.delimitedListToStringArray(String.format(argsFormat, port, port), " ")
			)
		);
		File f = new File("recycle/SampleAgent.cfg");
		if(f.exists()){
			f.delete();
		}
		f = new File("recycle/SampleAgent.bc");
		if(f.exists()){
			f.delete();
		}

		this.port = port;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		SecurityProtocols.getInstance().addDefaultProtocols();
		super.run();
		System.err.println("SNMP Agent Initialized: <localhost>:"+port);
	}
	@Override
	public void destroy() throws Exception {
		super.agent.shutdown();
		System.err.println("SNMP Agent Terminated: <localhost>:"+port);
	}
}

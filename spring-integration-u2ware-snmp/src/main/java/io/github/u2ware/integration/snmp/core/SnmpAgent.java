package io.github.u2ware.integration.snmp.core;

import java.io.File;
import java.util.Map;

import org.snmp4j.agent.example.SampleAgent;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.util.ArgumentParser;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;

public class SnmpAgent extends SampleAgent implements InitializingBean, DisposableBean{

	
	public static void main(String[] args) {
		
		int port = 10162;
		try{
			port = Integer.parseInt(args[0]);
		}catch(Exception e){
		}

		try {
			SnmpAgent.startup(port);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private static Map<Object, SnmpAgent> instance = Maps.newHashMap();
	
	public static void startup(int port) throws Exception{
		SnmpAgent value = new SnmpAgent(port);
		value.afterPropertiesSet();
		instance.put(port, value);
	}
	public static void shutdown(int port) throws Exception{
		instance.get(port).destroy();
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

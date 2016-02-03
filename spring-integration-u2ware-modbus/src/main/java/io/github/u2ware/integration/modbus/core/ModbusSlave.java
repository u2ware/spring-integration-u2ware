package io.github.u2ware.integration.modbus.core;

import java.util.Map;
import java.util.Random;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleDigitalIn;
import net.wimpi.modbus.procimg.SimpleDigitalOut;
import net.wimpi.modbus.procimg.SimpleInputRegister;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Maps;

public class ModbusSlave implements InitializingBean, DisposableBean{

	public static void main(String[] args) throws Exception{
		
		int port = 10000 + Modbus.DEFAULT_PORT;
		try{
			port = Integer.parseInt(args[0]);
		}catch(Exception e){
		}

		ModbusSlave.startup(port);
	}
	
	private static Map<Object, ModbusSlave> instance = Maps.newHashMap();
	
	public static void startup(int port) throws Exception{
		ModbusSlave value = new ModbusSlave();
		value.setLocalPort(port);
		value.afterPropertiesSet();
		instance.put(port, value);
	}
	public static void shutdown(int port) throws Exception{
		instance.get(port).destroy();
	}
	
	private int localPort = 10000 + Modbus.DEFAULT_PORT; //502
    private ModbusTCPListener listener = null;
	
	public int getLocalPort() {
		return localPort;
	}
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		SimpleProcessImage spi = null;

	      Random r = new Random();
	      
	      //1. prepare a process image
	      spi = new SimpleProcessImage();
	
	      
	      for(int i=0 ;i < 24; i++){
		      spi.addDigitalOut(new SimpleDigitalOut(r.nextBoolean()));
	      }
	      for(int i=0 ;i < 16; i++){
		      spi.addDigitalIn(new SimpleDigitalIn(r.nextBoolean()));
	      }
	
	      for(int i=0 ;i < 12; i++){
		      spi.addRegister(new SimpleRegister(randomRange(1, 251)));
	      }

	      for(int i=0 ;i < 6; i++){
		      spi.addInputRegister(new SimpleInputRegister(randomRange(1,45)));
	      }
	
	      //2. create the coupler holding the image
	      ModbusCoupler.getReference().setProcessImage(spi);
	      ModbusCoupler.getReference().setMaster(false);
	      ModbusCoupler.getReference().setUnitID(0);
	
	      //3. create a listener with 3 threads in pool
	      if (Modbus.debug) System.out.println("Listening...");
	      listener = new ModbusTCPListener(3);
	      listener.setPort(localPort);
	      listener.start();

	        System.err.println("Mobdus Slave Initialized: <localhost>:"+localPort);
	}    		
	
	@Override
	public void destroy() throws Exception {
		listener.stop();
		System.err.println("Mobdus Slave Terminated: <localhost>:"+localPort);
	}

	private int randomRange(int n1, int n2) {
	    return (int) (Math.random() * (n2 - n1 + 1)) + n1;
	}
}
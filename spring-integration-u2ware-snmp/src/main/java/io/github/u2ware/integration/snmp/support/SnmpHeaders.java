/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.u2ware.integration.snmp.support;


/**
 * Bacnet adapter specific message headers.
 *
 * @author kslee@u2ware.com
 * @since 1.0.0
 */
public class SnmpHeaders {
	
	private static final String PREFIX = "snmp_";
	
	public static final String LOCAL_PORT =  PREFIX + "localPort";
	public static final String LOCAL_MIB =  PREFIX + "localMib";

	public static final String REQUEST =  PREFIX + "request";
	
	private SnmpHeaders() {
		throw new AssertionError();
	}

}

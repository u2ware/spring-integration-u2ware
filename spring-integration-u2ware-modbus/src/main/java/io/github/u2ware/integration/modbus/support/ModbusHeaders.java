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

package io.github.u2ware.integration.modbus.support;

/**
 * Modbus adapter specific message headers.
 *
 * @author kslee@u2ware.com
 * @since 1.0.0
 */
public class ModbusHeaders {

	private static final String PREFIX = "modbus_";

	public static final String HOST = PREFIX + "host";
	public static final String PORT = PREFIX + "port";

	public static final String REQUEST = PREFIX + "request";

	
	/*
	public static final String FUNCTION_CODE = PREFIX + "functionCode";
	public static final String UNIT_ID   = PREFIX + "unitId";
	public static final String REFERENCE = PREFIX + "reference";
	public static final String COUNT     = PREFIX + "count";
*/
	
	/** Noninstantiable utility class */
	private ModbusHeaders() {
		throw new AssertionError();
	}

}

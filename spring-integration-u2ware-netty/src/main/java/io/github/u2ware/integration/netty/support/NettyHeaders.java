package io.github.u2ware.integration.netty.support;


/**
 * Netty adapter specific message headers.
 *
 * @author u2waremanager@gamil.com
 * @since 1.0
 */
public class NettyHeaders {

	private static final String PREFIX = "netty_";

	public static final String REMOTE_ADDRESS = PREFIX + "remote_address";

	public static final String HTTP_REQUEST_PATH = PREFIX + "http_request_path";
	public static final String HTTP_REQUEST_METHOD = PREFIX + "http_request_method";

	/** Noninstantiable utility class */
	private NettyHeaders() {
		throw new AssertionError();
	}

}

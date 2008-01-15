/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.ee;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

/**
 * A buffered servlet output stream. The buffering happens in memory via a
 * simple byte array output stream.
 * 
 * @author fcorneli
 * 
 */
public class BufferedServletOutputStream extends ServletOutputStream {

	private final ByteArrayOutputStream buffer;

	public BufferedServletOutputStream() {
		this.buffer = new ByteArrayOutputStream();
	}

	@Override
	public void write(int b) {
		this.buffer.write(b);
	}

	@Override
	public void close() throws IOException {
		this.buffer.close();
		super.close();
	}

	@Override
	public void flush() throws IOException {
		this.buffer.flush();
	}

	@Override
	public void write(byte[] b, int off, int len) {
		this.buffer.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		this.buffer.write(b);
	}

	/**
	 * Gives back the data that this servlet output stream has been buffering.
	 * 
	 */
	public byte[] getData() {
		byte[] data = this.buffer.toByteArray();
		return data;
	}
}

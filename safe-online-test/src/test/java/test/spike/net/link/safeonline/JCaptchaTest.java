/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;

public class JCaptchaTest {

	private static final Log LOG = LogFactory.getLog(JCaptchaTest.class);

	@Test
	public void testCaptcha() throws Exception {
		// setup
		String captchaId = UUID.randomUUID().toString();
		ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();

		// operate
		ImageCaptchaService captchaService = new DefaultManageableImageCaptchaService();
		BufferedImage challenge = (BufferedImage) captchaService
				.getChallengeForID(captchaId);
		challenge = (BufferedImage) captchaService.getChallengeForID(captchaId);
		ImageIO.write(challenge, "jpeg", jpegOutputStream);

		File tmpFile = File.createTempFile("captcha-", ".jpg");
		IOUtils.write(jpegOutputStream.toByteArray(), new FileOutputStream(
				tmpFile));
		LOG.debug("tmp file: " + tmpFile.getAbsolutePath());

		String result = JOptionPane.showInputDialog(null,
				"Give the content of the captcha from file "
						+ tmpFile.getAbsolutePath(), "Captcha Test",
				JOptionPane.OK_OPTION);
		LOG.debug("result: " + result);

		boolean valid = captchaService.validateResponseForID(captchaId, result);
		assertTrue(valid);

		/*
		 * Notice that we can only validate once.
		 */
	}
}

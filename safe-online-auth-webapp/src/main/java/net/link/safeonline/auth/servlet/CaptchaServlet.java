/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.octo.captcha.CaptchaException;
import com.octo.captcha.engine.image.gimpy.DefaultGimpyEngine;
import com.octo.captcha.service.captchastore.FastHashMapCaptchaStore;
import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;

/**
 * CAPTCHA servlet. We first tried out the JBoss Seam captcha component but
 * could not get it to work.
 * 
 * @author fcorneli
 * 
 */
public class CaptchaServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(CaptchaServlet.class);

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doGet");
		HttpSession session = request.getSession();
		String captchaId = session.getId();
		ImageCaptchaService captchaService = getCaptchaService(session);
		BufferedImage challengeImage;
		try {
			challengeImage = captchaService.getImageChallengeForID(captchaId);
		} catch (CaptchaException e) {
			LOG.error("CAPTCHA error: " + e.getMessage(), e);
			throw new ServletException("Could not generate CAPTCHA");
		}
		ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream();
		ImageIO.write(challengeImage, "jpeg", imageOutputStream);

		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");

		ServletOutputStream out = response.getOutputStream();
		IOUtils.write(imageOutputStream.toByteArray(), out);
		out.flush();
		out.close();
	}

	public static final String CAPTCHA_SERVICE_ATTRIBUTE = "CaptchaService";

	/**
	 * Because exactly the same captcha service instance is required for
	 * validation we need to store the captcha service within the session.
	 * 
	 * @param session
	 */
	private ImageCaptchaService getCaptchaService(HttpSession session) {
		ImageCaptchaService captchaService = (ImageCaptchaService) session
				.getAttribute(CAPTCHA_SERVICE_ATTRIBUTE);
		if (null == captchaService) {
			captchaService = new DefaultManageableImageCaptchaService(
					new FastHashMapCaptchaStore(), new DefaultGimpyEngine(),
					180, 100000, 75000);
			session.setAttribute(CAPTCHA_SERVICE_ATTRIBUTE, captchaService);
		}
		return captchaService;
	}
}

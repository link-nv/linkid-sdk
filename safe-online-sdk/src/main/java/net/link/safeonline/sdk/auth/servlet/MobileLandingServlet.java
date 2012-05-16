package net.link.safeonline.sdk.auth.servlet;

import com.google.common.collect.Maps;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OauthInvalidMessageException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.*;
import net.link.safeonline.sdk.servlet.AbstractConfidentialLinkIDInjectionServlet;
import net.link.util.error.ValidationFailedException;
import net.link.util.servlet.annotation.Init;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * TODO description
 * <p/>
 * Date: 15/05/12
 * Time: 15:27
 *
 * @author: sgdesmet
 */
public class MobileLandingServlet extends AbstractConfidentialLinkIDInjectionServlet {

    private static final Log LOG = LogFactory.getLog( MobileLandingServlet.class );

    private static final int QR_SIZE = 512;

    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        delegate( request, response );
    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        delegate( request, response );
    }

    /**
     * create authentication context and start login
     * @param request
     * @param response
     */
    public void delegate(final HttpServletRequest request, final HttpServletResponse response) {
        ResponseMessage responseMessage = null;
        try {
            responseMessage = MessageUtils.getAuthorizationCodeResponse( request );
        }
        catch (OauthInvalidMessageException e) {
            throw new InternalInconsistencyException( "Bad response message: ", e );
        }

        if (responseMessage instanceof AuthorizationCodeResponse){
            AuthorizationCodeResponse authorizationCodeResponse = (AuthorizationCodeResponse) responseMessage;
            try {
                showCodeAndState( request, response, authorizationCodeResponse.getCode(), authorizationCodeResponse.getState() );
            }
            catch (IOException e) {
                throw new InternalInconsistencyException( e );
            }
        } else
            throw new InternalInconsistencyException( "Bad response message" ); //TODO cleaner error handling
    }

    protected void showCodeAndState(final HttpServletRequest request, final HttpServletResponse response, String code, String state)
            throws IOException {
        String data = "code=" + code;
        if (state != null && state.length() != 0)
            data = data + "&state=" + state;
        BitMatrix qrCode = generateQRCode( data );
        response.setContentType( "image/jpeg" );
        response.setHeader( "Cache-Control", "no-store" );
        response.setHeader( "Pragma", "no-cache" );
        OutputStream outputStream = response.getOutputStream();
        MatrixToImageWriter.writeToStream( qrCode, "jpg", outputStream );
    }

    private static BitMatrix generateQRCode(final String qrCodeContent) {

        LOG.debug( "generate QR code: content=" + qrCodeContent );

        QRCodeWriter writer = new QRCodeWriter();
        try {
            int height = QR_SIZE;
            int width = QR_SIZE;
            Map<EncodeHintType, Object> hints = Maps.newHashMap();
            hints.put( EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H );
            return writer.encode( qrCodeContent, BarcodeFormat.QR_CODE, width, height, hints );
        }
        catch (WriterException e) {
            throw new InternalInconsistencyException( e );
        }
    }

}

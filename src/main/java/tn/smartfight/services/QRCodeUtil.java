package tn.smartfight.services;

import tn.smartfight.models.*;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QRCodeUtil {

    // ---------------------------------------------------------------
    // generateQRImage
    // ---------------------------------------------------------------
    public static Image generateQRImage(String data, int sizePx) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, sizePx, sizePx, hints);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] bytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return new Image(bais);
        } catch (WriterException | IOException e) {
            System.err.println("[QRCodeUtil] generateQRImage: " + e.getMessage());
            return null;
        }
    }

    // ---------------------------------------------------------------
    // buildQRData
    // ---------------------------------------------------------------
    public static String buildQRData(EventBooking b) {
        return "SMARTFIGHT" +
               "|REF:" + b.getBookingReference() +
               "|EVENT:" + b.getEventId() +
               "|USER:" + b.getUserId() +
               "|TYPE:" + b.getTicketType() +
               "|QTY:" + b.getTicketQuantity();
    }
}

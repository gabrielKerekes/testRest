package ocrahotp;

import java.math.BigInteger;

/**
 * Created by GabrielK on 10-Feb-17.
 */

public class Ocra extends OcraHotp {
    private static final int OCRA_LENGTH = 6;

    private static byte[] hexStr2Bytes(String hex) {
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    public static String generateOCRA(String imei, String pin, String otp, String hexQuestion) {
        // pad question
        while (hexQuestion.length() < 256) {
            hexQuestion = hexQuestion + "0";
        }

        String hexKey = imei + pin + otp;

        byte[] keyBytes = hexStr2Bytes(hexKey);
        byte[] questionBytes = hexStr2Bytes(hexQuestion);

        byte[] hash = generateHmac(keyBytes, questionBytes);

        int offset = hash[hash.length - 1] & 0xf;
        int binary = ((hash[offset] & 0x7f) << 24) |
                    ((hash[offset + 1] & 0xff) << 16) |
                    ((hash[offset + 2] & 0xff) << 8) |
                    (hash[offset + 3] & 0xff);

        int ocraOtp = binary % DIGITS_POWER[OCRA_LENGTH];

        String result = Integer.toString(ocraOtp);
        // pad result
        while (result.length() < OCRA_LENGTH) {
            result = "0" + result;
        }

        return result;
    }
}

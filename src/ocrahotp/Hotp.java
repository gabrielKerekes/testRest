package ocrahotp;

import java.math.BigInteger;

public class Hotp extends OcraHotp {
    private static final int[] doubleDigits = { 0, 2, 4, 6, 8, 1, 3, 5, 7, 9 };

    public static String GenerateOTP(String imei, String pin, long movingFactor, int codeDigits, boolean addChecksum, int truncationOffset) {
        String hexImei = String.format("%040x", new BigInteger(1, imei.getBytes()));
        String hexPin = String.format("%040x", new BigInteger(1, pin.getBytes()));
        byte[] secret = generateHmac(hexImei.getBytes(), hexPin.getBytes());

        return GenerateOTP(secret, movingFactor, codeDigits, addChecksum, truncationOffset);
    }

    public static String GenerateOTP(byte[] secret, long movingFactor, int codeDigits, boolean addChecksum, int truncationOffset) {
        // put movingFactor value into text byte array
        String result = null;
        int digits = addChecksum ? (codeDigits + 1) : codeDigits;
        byte[] text = new byte[8];
        for (int i = text.length - 1; i >= 0; i--) {
            text[i] = (byte) (movingFactor & 0xff);
            movingFactor >>= 8;
        }

        // compute hmac hash
        byte[] hash = generateHmac(secret, text);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;
        if ( (0<=truncationOffset) &&
                (truncationOffset<(hash.length-4)) ) {
            offset = truncationOffset;
        }
        int binary =
                ((hash[offset] & 0x7f) << 24)
                        | ((hash[offset + 1] & 0xff) << 16)
                        | ((hash[offset + 2] & 0xff) << 8)
                        | (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];
        if (addChecksum) {
            otp =  (otp * 10) + calcChecksum(otp, codeDigits);
        }
        result = Integer.toString(otp);
        while (result.length() < digits) {
            result = "0" + result;
        }
        return result;
    }

    private static int calcChecksum(long num, int digits) {
        boolean doubleDigit = true;
        int     total = 0;
        while (0 < digits--) {
            int digit = (int) (num % 10);
            num /= 10;
            if (doubleDigit) {
                digit = doubleDigits[digit];
            }
            total += digit;
            doubleDigit = !doubleDigit;
        }
        int result = total % 10;
        if (result > 0) {
            result = 10 - result;
        }
        return result;
    }


}
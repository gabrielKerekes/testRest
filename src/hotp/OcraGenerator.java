package hotp;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;

public class OcraGenerator 
{
    private static byte[] hmac_sha1(String crypto, byte[] keyBytes, byte[] text)
    {
        Mac hmac;
        try
        {
            hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");

            hmac.init(macKey);

            return hmac.doFinal(text);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }


    private static final int[] DIGITS_POWER = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    private static byte[] hexStr2Bytes(String hex)
    {
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    public static String generateOCRA(String key, String question)
    {
        int codeDigits;
        String result;
        int questionLength;

        codeDigits = 6;

        // Question - always 128 bytes
        while (question.length() < 256)
        {
            question = question + "0";
        }
        questionLength = 128;

        byte[] msg = new byte[questionLength + 1];
        byte[] bArray;

        if (questionLength > 0)
        {
            bArray = hexStr2Bytes(question);
            System.arraycopy(bArray, 0, msg, 0, bArray.length);
        }

        bArray = hexStr2Bytes(key);

        byte[] hash = hmac_sha1("HmacSHA256", bArray, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary =
                ((hash[offset] & 0x7f) << 24) |
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];

        result = Integer.toString(otp);
        while (result.length() < codeDigits)
        {
            result = "0" + result;
        }

        return result;
    }
}

package com.example.api.testutil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public final class JwtTestUtils {

    private static final String SECRET = "0123456789_0123456789_0123456789_01";

    private JwtTestUtils() {}

    public static String createTestJwt() {
        String headerJson = """
                {"alg":"HS256","typ":"JWT"}
                """;

        String payloadJson = """
                {"sub":"test-user"}
                """;

        String header = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(headerJson.getBytes());

        String payload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payloadJson.getBytes());

        String unsignedToken = header + "." + payload;

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(), "HmacSHA256"));
            byte[] signatureBytes = mac.doFinal(unsignedToken.getBytes());

            String signature = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(signatureBytes);

            return unsignedToken + "." + signature;
        } catch (Exception ex) {
            throw new RuntimeException("Error while creating test JWT", ex);
        }
    }
}

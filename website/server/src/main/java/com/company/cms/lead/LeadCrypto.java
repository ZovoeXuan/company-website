package com.company.cms.lead;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 咨询敏感字段处理：
 * - 手机号使用 AES-256-GCM 认证加密，密文信封为「版本 1 字节 + 12 字节 nonce + ciphertext/tag」；
 * - 幂等载荷与限流键使用 HMAC-SHA256 摘要；
 * - 两个密钥只从环境变量加载，不进数据库、不进源码。
 */
@Component
public class LeadCrypto {

    private static final byte ENVELOPE_VERSION = 1;
    private static final int NONCE_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec encryptionKey;
    private final SecretKeySpec hashKey;
    private final SecureRandom random = new SecureRandom();

    public LeadCrypto(@Value("${cms.lead.encryption-key:}") String encryptionKeyBase64,
                      @Value("${cms.lead.hash-key:}") String hashKeyBase64) {
        this.encryptionKey = key(encryptionKeyBase64, "CMS_LEAD_ENCRYPTION_KEY");
        this.hashKey = key(hashKeyBase64, "CMS_LEAD_HASH_KEY");
    }

    private SecretKeySpec key(String raw, String name) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException(name + " 未配置，咨询线索无法安全存储");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(name + " 必须是 Base64 编码的 32 字节密钥");
        }
        if (bytes.length != 32) {
            throw new IllegalStateException(name + " 长度必须是 32 字节");
        }
        return new SecretKeySpec(bytes, "AES");
    }

    public byte[] encryptPhone(String phone) {
        try {
            byte[] nonce = new byte[NONCE_BYTES];
            random.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(TAG_BITS, nonce));
            byte[] sealed = cipher.doFinal(phone.getBytes(StandardCharsets.UTF_8));
            byte[] envelope = new byte[1 + NONCE_BYTES + sealed.length];
            envelope[0] = ENVELOPE_VERSION;
            System.arraycopy(nonce, 0, envelope, 1, NONCE_BYTES);
            System.arraycopy(sealed, 0, envelope, 1 + NONCE_BYTES, sealed.length);
            return envelope;
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("手机号加密失败", ex);
        }
    }

    public String decryptPhone(byte[] envelope) {
        if (envelope == null || envelope.length < 1 + NONCE_BYTES + 16 || envelope[0] != ENVELOPE_VERSION) {
            throw new IllegalStateException("手机号密文格式不正确");
        }
        try {
            byte[] nonce = new byte[NONCE_BYTES];
            System.arraycopy(envelope, 1, nonce, 0, NONCE_BYTES);
            byte[] sealed = new byte[envelope.length - 1 - NONCE_BYTES];
            System.arraycopy(envelope, 1 + NONCE_BYTES, sealed, 0, sealed.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(TAG_BITS, nonce));
            return new String(cipher.doFinal(sealed), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("手机号解密失败", ex);
        }
    }

    public byte[] payloadHash(String canonicalPayload) {
        return hmac(canonicalPayload.getBytes(StandardCharsets.UTF_8));
    }

    /** 限流键使用服务端密钥摘要，不保存可枚举的普通手机号哈希。 */
    public String digest(String value) {
        return HexFormat.of().formatHex(hmac(value.getBytes(StandardCharsets.UTF_8)));
    }

    private byte[] hmac(byte[] input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hashKey);
            return mac.doFinal(input);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("摘要计算失败", ex);
        }
    }
}

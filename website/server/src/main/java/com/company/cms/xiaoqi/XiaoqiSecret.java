package com.company.cms.xiaoqi;

/** 小鑫接入配置；apiKey 为解密后的明文，只在服务端内存中使用。 */
public record XiaoqiSecret(String apiUrl, String apiKey, String apiKeyLast4, String authHeader,
                           String authPrefix, String modelName, String systemPrompt, int timeoutMs) {

    public boolean configured() {
        return apiUrl != null && !apiUrl.isBlank();
    }
}

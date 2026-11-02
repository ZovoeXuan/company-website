package com.company.cms.media;

import java.io.IOException;

/**
 * 只读文件头判断图片格式与尺寸，不做整图解码：
 * 既避免解压炸弹，也不依赖 ImageIO 是否支持 WebP。
 */
public final class ImageInspector {

    public record ImageInfo(String extension, String mimeType, int width, int height) {
    }

    private ImageInspector() {
    }

    public static ImageInfo inspect(byte[] data) throws IOException {
        if (data == null || data.length < 16) {
            throw new IOException("文件内容过短");
        }
        if ((data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return png(data);
        }
        if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8) {
            return jpeg(data);
        }
        if (isWebp(data)) {
            return webp(data);
        }
        throw new IOException("不是受支持的图片格式");
    }

    private static ImageInfo png(byte[] data) throws IOException {
        if (data.length < 24) {
            throw new IOException("PNG 头部不完整");
        }
        int width = readInt(data, 16);
        int height = readInt(data, 20);
        return new ImageInfo("png", "image/png", width, height);
    }

    private static ImageInfo jpeg(byte[] data) throws IOException {
        int index = 2;
        while (index + 9 < data.length) {
            if ((data[index] & 0xFF) != 0xFF) {
                index++;
                continue;
            }
            int marker = data[index + 1] & 0xFF;
            // SOF0..SOF15，排除 DHT(C4)、JPG(C8)、DAC(CC)
            if (marker >= 0xC0 && marker <= 0xCF && marker != 0xC4 && marker != 0xC8 && marker != 0xCC) {
                int height = ((data[index + 5] & 0xFF) << 8) | (data[index + 6] & 0xFF);
                int width = ((data[index + 7] & 0xFF) << 8) | (data[index + 8] & 0xFF);
                return new ImageInfo("jpg", "image/jpeg", width, height);
            }
            int length = ((data[index + 2] & 0xFF) << 8) | (data[index + 3] & 0xFF);
            if (length < 2) {
                throw new IOException("JPEG 段长度异常");
            }
            index += 2 + length;
        }
        throw new IOException("未找到 JPEG 尺寸信息");
    }

    private static boolean isWebp(byte[] data) {
        return data.length >= 16
                && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P';
    }

    private static ImageInfo webp(byte[] data) throws IOException {
        String chunk = new String(data, 12, 4, java.nio.charset.StandardCharsets.US_ASCII);
        int width;
        int height;
        switch (chunk) {
            case "VP8 " -> {
                if (data.length < 30) {
                    throw new IOException("WebP 头部不完整");
                }
                width = (((data[26] & 0xFF) | ((data[27] & 0xFF) << 8)) & 0x3FFF);
                height = (((data[28] & 0xFF) | ((data[29] & 0xFF) << 8)) & 0x3FFF);
            }
            case "VP8L" -> {
                if (data.length < 25) {
                    throw new IOException("WebP 头部不完整");
                }
                int bits = (data[21] & 0xFF) | ((data[22] & 0xFF) << 8)
                        | ((data[23] & 0xFF) << 16) | ((data[24] & 0xFF) << 24);
                width = (bits & 0x3FFF) + 1;
                height = ((bits >> 14) & 0x3FFF) + 1;
            }
            case "VP8X" -> {
                if (data.length < 30) {
                    throw new IOException("WebP 头部不完整");
                }
                width = ((data[24] & 0xFF) | ((data[25] & 0xFF) << 8) | ((data[26] & 0xFF) << 16)) + 1;
                height = ((data[27] & 0xFF) | ((data[28] & 0xFF) << 8) | ((data[29] & 0xFF) << 16)) + 1;
            }
            default -> throw new IOException("未知的 WebP 子格式");
        }
        return new ImageInfo("webp", "image/webp", width, height);
    }

    private static int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
    }
}

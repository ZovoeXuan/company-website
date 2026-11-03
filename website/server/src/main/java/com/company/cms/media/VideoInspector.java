package com.company.cms.media;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 只读文件头识别视频格式。视频尺寸与时长由前端 metadata 读取，
 * 服务端不解析 MP4/WebM 容器，避免引入额外解析依赖。
 */
public final class VideoInspector {

    public record VideoInfo(String extension, String mimeType) {
    }

    private VideoInspector() {
    }

    public static VideoInfo inspect(byte[] data) throws IOException {
        if (data == null || data.length < 12) {
            throw new IOException("文件内容过短");
        }
        if (isMp4(data)) {
            return new VideoInfo("mp4", "video/mp4");
        }
        if (isWebm(data)) {
            return new VideoInfo("webm", "video/webm");
        }
        throw new IOException("不是受支持的视频格式");
    }

    private static boolean isMp4(byte[] data) {
        String box = new String(data, 4, 4, StandardCharsets.US_ASCII);
        return "ftyp".equals(box);
    }

    private static boolean isWebm(byte[] data) {
        return (data[0] & 0xFF) == 0x1A && (data[1] & 0xFF) == 0x45
                && (data[2] & 0xFF) == 0xDF && (data[3] & 0xFF) == 0xA3;
    }
}

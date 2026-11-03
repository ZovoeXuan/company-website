package com.company.cms.media;

import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * 素材直写响应：绕过消息转换器，支持 Range 与流式传输，视频拖动进度条时不整文件重传。
 * 单次最多返回 8 MB，避免一次 Range 请求占用过多内存。
 */
public final class MediaHttp {

    private static final long MAX_CHUNK = 8L * 1024 * 1024;

    private MediaHttp() {
    }

    public static void write(HttpServletResponse response, MediaService.StoredFile file,
                             String rangeHeader, String cacheControl, boolean inlineName) throws IOException {
        long length = Files.size(file.path());
        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
        response.setContentType(file.mimeType());
        response.setHeader(HttpHeaders.CACHE_CONTROL, cacheControl);
        if (inlineName) {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename*=UTF-8''" + encode(file.originalName()));
        }

        List<HttpRange> ranges = null;
        if (rangeHeader != null && !rangeHeader.isBlank()) {
            try {
                ranges = HttpRange.parseRanges(rangeHeader);
            } catch (IllegalArgumentException ex) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes */" + length);
                return;
            }
        }

        if (ranges != null && !ranges.isEmpty()) {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(length);
            long end = range.getRangeEnd(length);
            if (start < 0 || start >= length || end < start) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes */" + length);
                return;
            }
            end = Math.min(end, start + MAX_CHUNK - 1);
            long count = end - start + 1;
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + length);
            response.setContentLengthLong(count);
            copyRange(file, start, count, response.getOutputStream());
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentLengthLong(length);
        try (InputStream in = Files.newInputStream(file.path());
             OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
        }
    }

    private static void copyRange(MediaService.StoredFile file, long start, long count,
                                  OutputStream out) throws IOException {
        try (InputStream in = Files.newInputStream(file.path())) {
            long skipped = 0;
            while (skipped < start) {
                long step = in.skip(start - skipped);
                if (step <= 0) {
                    throw new ApiException(ErrorCode.NOT_FOUND, "素材文件读取失败");
                }
                skipped += step;
            }
            byte[] buffer = new byte[8192];
            long remaining = count;
            while (remaining > 0) {
                int read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read < 0) {
                    break;
                }
                out.write(buffer, 0, read);
                remaining -= read;
            }
        }
    }

    private static String encode(String name) {
        return java.net.URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
    }
}

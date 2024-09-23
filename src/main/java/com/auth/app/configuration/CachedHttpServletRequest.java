package com.auth.app.configuration;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.*;


public class CachedHttpServletRequest extends HttpServletRequestWrapper {
    public CachedHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        InputStream requestInputStream = request.getInputStream();
        cachedPayload = StreamUtils.copyToByteArray(requestInputStream);
    }
    private final byte[] cachedPayload;


    @Override
    public ServletInputStream getInputStream() {
        return new CachedServletInputStream(cachedPayload);
    }

    @Override
    public BufferedReader getReader() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedPayload);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream));
    }
}



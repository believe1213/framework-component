package com.minister.framework.boot.filter.utils;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 处理 http post 请求 Wrapper
 *
 * @author QIUCHANGQING620
 * @date 2024-04-28 00:30
 */
public class CustomServletRequestWrapper extends HttpServletRequestWrapper {

    protected byte[] body;

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public CustomServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        if (FilterUtils.needRequestWrapper(request)) {
            body = getBodyRequest(request.getReader(), request.getCharacterEncoding());
        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bs = new ByteArrayInputStream(body == null ? new byte[]{} : body);
        return new ServletInputStream() {

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return bs.read();
            }
        };
    }

    private byte[] getBodyRequest(BufferedReader br, String encode) throws IOException {
        String temp = "";
        String body = "";
        while ((temp = br.readLine()) != null) {
            body += temp + "\r\n";
        }
        if (StringUtils.isNotEmpty(encode)) {
            return body.getBytes(encode);
        }
        return body.getBytes();
    }

    // 二进制读取
    private byte[] readAsBytes(HttpServletRequest request) {
        try (ServletInputStream in = request.getInputStream();) {
            int len = request.getContentLength();
            byte[] buffer = new byte[len];
            in.read(buffer, 0, len);
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

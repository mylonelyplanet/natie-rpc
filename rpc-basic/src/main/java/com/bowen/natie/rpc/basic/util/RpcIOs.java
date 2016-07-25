package com.bowen.natie.rpc.basic.util;

import org.apache.commons.io.output.StringBuilderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.ZipFile;

/**
 * Created by mylonelyplanet on 16/7/25.
 */
public class RpcIOs {
    private static final String ERROR_MESSAGE = "error when close the closeable";
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private static Logger logger = LoggerFactory.getLogger(RpcIOs.class);

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                logger.error(ERROR_MESSAGE, e);
            }
        }
    }

    /**
     * For JDK6 which ZipFile is not Closeable.
     */
    public static void closeQuietly(ZipFile closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                logger.error(ERROR_MESSAGE, e);
            }
        }
    }

    /**
     * For JDK6 which Socket is not Closeable.
     */
    public static void closeQuietly(Socket closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                logger.error(ERROR_MESSAGE, e);
            }
        }
    }

    public static String toString(URL url) throws IOException {
        InputStream inputStream = url.openStream();
        try {
            return toString(inputStream);
        } finally {
            inputStream.close();
        }
    }

    public static String toString(InputStream input) throws IOException {
        StringBuilderWriter sw = new StringBuilderWriter();
        copy(input, sw);
        return sw.toString();
    }

    public static void copy(InputStream input, Writer output) throws IOException {
        InputStreamReader in = new InputStreamReader(input, Charset.defaultCharset());
        copy(in, output);
    }

    public static int copy(Reader input, Writer output) throws IOException {

        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return (int) count;
    }
}

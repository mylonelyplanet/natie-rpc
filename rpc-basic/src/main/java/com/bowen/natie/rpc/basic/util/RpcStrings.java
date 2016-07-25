package com.bowen.natie.rpc.basic.registry.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mylonelyplanet on 16/7/24.
 */
public class RpcStrings {

    public static List<String> split(final String str, final char separatorChar) {
        if (str == null) {
            return Collections.emptyList();
        }
        final int len = str.length();
        if (len == 0) {
            return Collections.emptyList();
        }
        final List<String> list = new ArrayList<String>();
        int i = 0, start = 0;
        boolean match = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match) {
            list.add(str.substring(start, i));
        }
        return list;

    }

    public static List<String> split(final String str, final char separatorChar, int expectParts) {
        if (str == null) {
            return Collections.emptyList();
        }
        final int len = str.length();
        if (len == 0) {
            return Collections.emptyList();
        }
        final List<String> list = new ArrayList<String>(expectParts);
        int i = 0, start = 0;
        boolean match = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match) {
            list.add(str.substring(start, i));
        }
        return list;

    }
}

package com.dew.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class VPNUtil {

    public static void reconnectMullvad() {
        try {
            ProcessBuilder builder = new ProcessBuilder("mullvad", "reconnect");
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                LogUtil.infoLog(line);
            }

            process.waitFor();
        } catch (Exception e) {
            LogUtil.infoLog(String.valueOf(e));
        }
    }
}

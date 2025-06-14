package com.dew.utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MiscUtil {

    public static void showURL(final String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (final IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}

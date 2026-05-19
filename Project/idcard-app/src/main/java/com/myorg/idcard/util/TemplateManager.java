package com.myorg.idcard.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateManager {
    private static final String TPL_PATH = "/templates/";

    // Return list of template file names found in resources/templates
    public static List<String> listTemplates() {
        try {
            // Using ClassLoader to list resource names is tricky when packaged in jar.
            // We'll try reading from classpath directory via resource listing (works in IDE),
            // and fallback to a hard-coded known list if needed.
            InputStream index = TemplateManager.class.getResourceAsStream(TPL_PATH);
            // Most environments don't allow directory listing via getResourceAsStream.
            // Instead we will scan a small manifest file approach — BUT simpler:
            // We'll attempt to read from the resources by checking known filenames (common).
        } catch (Exception ex) {
            // ignore
        }
        // Simpler robust approach: attempt to find a few common names, or return whatever exists in resources by trying to load typical names.
        // For your project, ensure templates folder contains files and also create a small index file if you prefer.
        // We'll scan classpath resources by using the system resource URL approach:
        try {
            // Try get resources via ClassLoader (works when running in IDE)
            Enumeration<java.net.URL> roots = TemplateManager.class.getClassLoader().getResources("templates");
            while (roots.hasMoreElements()) {
                java.net.URL url = roots.nextElement();
                if (url.getProtocol().equals("file")) {
                    java.nio.file.Path p = java.nio.file.Paths.get(url.toURI());
                    try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(p)) {
                        return stream.map(x -> x.getFileName().toString()).filter(n -> n.toLowerCase().endsWith(".png")).collect(Collectors.toList());
                    }
                } else if (url.getProtocol().equals("jar")) {
                    // jar protocol: list entries
                    String path = url.getPath();
                    String jarPath = path.substring(5, path.indexOf("!"));
                    try (java.util.jar.JarFile jar = new java.util.jar.JarFile(java.net.URLDecoder.decode(jarPath, "UTF-8"))) {
                        List<String> names = new ArrayList<>();
                        var entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            var e = entries.nextElement();
                            String n = e.getName();
                            if (n.startsWith("templates/") && n.toLowerCase().endsWith(".png")) {
                                names.add(n.substring("templates/".length()));
                            }
                        }
                        return names;
                    }
                }
            }
        } catch (Exception ex) {
            // fallback
        }
        // fallback: try a few defaults
        return Arrays.asList("idcard_template.png");
    }

    public static BufferedImage loadTemplateImage(String name) throws IOException {
        InputStream in = TemplateManager.class.getResourceAsStream(TPL_PATH + name);
        if (in == null) throw new IOException("Template not found: " + name);
        return ImageIO.read(in);
    }
}

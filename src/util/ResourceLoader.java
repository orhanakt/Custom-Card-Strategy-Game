package util;

import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

//Loads image resources (cards, icons) from class path and caches them for reuse.
public final class ResourceLoader {

    private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();

    private ResourceLoader() 
    {
    	
    }

    public static URL getResource(String path) 
    {
        if (path == null) return null;
        String p = path.trim();
        if (p.isEmpty()) return null;
        if (!p.startsWith("/")) p = "/" + p;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = (cl == null) ? null : cl.getResource(p.substring(1));
        if (url == null) url = ResourceLoader.class.getResource(p);
        return url;
    }

    public static ImageIcon getIcon(String path) 
    {
        if (path == null) return null;
        String p = path.trim();
        if (p.isEmpty()) return null;
        if (!p.startsWith("/")) p = "/" + p;
        ImageIcon cached = ICON_CACHE.get(p);
        if (cached != null) return cached;
        URL url = getResource(p);
        if (url == null) return null;
        ImageIcon icon = new ImageIcon(url);
        ICON_CACHE.put(p, icon);
        return icon;
    }

    public static ImageIcon getScaledIcon(String path, int w, int h) 
    {
        ImageIcon icon = getIcon(path);
        if (icon == null) return null;
        int width = Math.max(1, w);
        int height = Math.max(1, h);
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}

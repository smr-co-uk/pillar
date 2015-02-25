package com.chrisomeara.pillar;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClassPathUtil
 *  
 * @author Peter Lappo
 *
 */

public class ClassPathUtil {

  private static Logger log = LoggerFactory.getLogger(ClassPathUtil.class);

  /**
   * List directory contents from the classpath. Not recursive. This is basically a brute-force implementation. Works
   * for regular files and also JARs.
   * 
   * For example:
   * ClassPathUtil.getResourceListing(this.getClass(), "cql/migrations");
   * 
   * @param clazz
   *          Any java class that lives in the same place as the resources you want.
   * @param path
   *          Can end with "/", but doesn't have to. 
   *          Should not start with "/" as we search from the root directory anyway.
   * @return List of URLs for each resource found in a resource directory or jar.
   * 
   * @throws URISyntaxException
   * @throws IOException
   * 
   * @author Peter Lappo
   */
  public static List<URL> getResourceListing(Class<?> clazz, String path) throws URISyntaxException, IOException {
    if (!path.endsWith("/")) {
      path = path + '/';
    }
    Enumeration<URL> dirURLs = clazz.getClassLoader().getResources(path);
    List<URL> result = new ArrayList<URL>();
    
    while (dirURLs.hasMoreElements()) {
      URL url = dirURLs.nextElement();
      log.info("Checking url: " + url);
      
      if (url.getProtocol().equals("file")) {
        File file = new File(url.getFile());
        if (file.isDirectory()) {
          // enumerate files in directory
          for (String entrystr :file.list()) {
            result.add(new URL(url.toExternalForm() + entrystr));
          }
        } else {
          result.add(url);
        }
      }

      if (url.getProtocol().equals("jar")) {
        if (url.toExternalForm().contains("sources")) {
          log.warn("Ignoring sources url: " + url);
          continue; // ignore sources
        }
        /* A JAR path */
        String jarPath = url.getPath().substring(5, url.getPath().indexOf("!")); // strip out only the JAR file
        JarFile jar = null;
        try {
          jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
          Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
          while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(path)) { // filter according to the path
              if (entry.isDirectory()) {
                log.warn("Ignoring subdirectory url: " + entry);
                continue; // ignore subdirectories
              }
              String entrystr = name.substring(path.length());
              result.add(new URL(url.toExternalForm() + entrystr));
            }
          }
        } finally {
          if (jar != null) {
            jar.close();
          }
        }
      }
    }

    return result;
  }

}

package spinat.super22;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Main {

    static void copyRessourcetoFile(String ressource, File f) throws IOException {
        ClassLoader cl = (Main.class).getClassLoader();
        try (InputStream ins = cl.getResourceAsStream(ressource);
                OutputStream os = new FileOutputStream(f)) {
            byte[] buf = new byte[1024 * 1024 * 50];
            while (true) {
                int count = ins.read(buf);
                if (count < 0) {
                    break;
                }
                os.write(buf, 0, count);
            }
        }
    }

    private static String findAttributeString(String attr) throws IOException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader()
                .getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            URL manifestUrl = resources.nextElement();
            Manifest manifest = new Manifest(manifestUrl.openStream());
            Attributes mainAttributes = manifest.getMainAttributes();
            return mainAttributes.getValue(attr);
        }
        throw new RuntimeException("no port attribute found");
    }

    static void startBrowser(int port) throws MalformedURLException, IOException, InterruptedException, URISyntaxException {
        while (true) {
            try {
                URL url = new URL("http://localhost:" + port);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.getContent();
                break;
            } catch (Exception e) {
                Thread.sleep(500);
            }
        }
        java.awt.Desktop.getDesktop().browse(new URI("http://localhost:" + port));
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final File fjar = java.io.File.createTempFile("jetty", ".jar");
        fjar.deleteOnExit();
        copyRessourcetoFile("jetty-runner.jar", fjar);
        final File fwar = java.io.File.createTempFile("app", ".war");
        fwar.deleteOnExit();
        copyRessourcetoFile("warfile.war", fwar);
        URL u = fjar.toURI().toURL();
        ClassLoader cl = new URLClassLoader(new URL[]{u});
        Thread.currentThread().setContextClassLoader(cl);
        JarFile jf = new JarFile(fjar);
        Attributes a = jf.getManifest().getMainAttributes();
        String mainClass = a.getValue(Attributes.Name.MAIN_CLASS);
        Class c = cl.loadClass(mainClass);
        Method m = c.getMethod("main", new Class[]{args.getClass()});
        String portStr = findAttributeString("port");
        int port = Integer.parseInt(portStr);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startBrowser(port);
                } catch (Exception ex) {
                    System.err.println(ex);
                    ex.printStackTrace(System.err);
                }
            }
        });
        t.start();
        String[] jettyargs = new String[]{"--port", "" + port, "--host", "127.0.0.1", fwar.toString()};
        m.invoke(null, new Object[]{jettyargs});
    }
}

package com.eb2.cachingproxy;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class CachingProxyApplication {

    private static String origin;
    private static int port;


    public static void main(String[] args) {

        Map<String, String> params = parseArgs(args);
        if (params.containsKey("help")) {
            showHelp();
            return;
        }

        if (params.containsKey("port")){
            port = Integer.parseInt(params.get("port"));
            System.setProperty("server.port", String.valueOf(port));
        }
        if (params.containsKey("origin")){
            origin = params.get("origin");
            System.setProperty("proxy.origin", origin);
        }


        if (params.containsKey("clear-cache")){
            try {
                new RestTemplate().postForLocation("http://localhost: " + port + "/clear-cache", null);
                System.out.println("Cache cleared successfully.");
            }catch (Exception e){
                System.err.println("Failed to clear cache: " + e.getMessage());
            }
            return;
        }


        new SpringApplicationBuilder(CachingProxyApplication.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=" + port, "proxy.origin=" + origin)
                .run(args);
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    params.put(key, args[++i]);
                }else {
                    params.put(key, "");
                }
            }
        }
        return params;
    }

    private static void showHelp() {
        System.out.println("Usage:");
        System.out.println("  caching-proxy --port <number> --origin <url>");
        System.out.println("  caching-proxy --clear-cache");
        System.out.println("Options:");
        System.out.println("  --port <number>    Specify the server port (default: 3000)");
        System.out.println("  --origin <url>     Specify the proxy origin URL");
        System.out.println("  --clear-cache      Clear the cache and exit");
        System.out.println("  --help             Show this help message");
    }


}

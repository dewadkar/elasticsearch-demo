package elasticsearch;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;

/**
 * @author dewadkar
 * elasticsearch elasticsearch
 * Mar 4, 2015 4:20:55 PM
 * 
 */
public class NetworkUtil {

    private static final Log LOG = LogFactory.getLog(NetworkUtil.class);
    private static String EMPTY_STRING = "";
    private static final HttpClient HTTP_CLIENT;

    static {
        LOG.debug("Initializing HTTP Client");

        HTTP_CLIENT = HttpClientBuilder.create().build();

        LOG.info("Completed initializing HTTP Client");
    }

    public static String curl(String sUrl, String method, String data) {

        String content = "";
        try {

            HttpRequestBase base = null;

            if ("PUT".equalsIgnoreCase(method)) {
                HttpPut httpPut = new HttpPut(getString(sUrl).replace(" ", "%20"));
                httpPut.setEntity(new StringEntity(data, "UTF-8"));
                base = httpPut;
            } else if ("POST".equalsIgnoreCase(method)) {
                HttpPost httpPost = new HttpPost(getString(sUrl).replace(" ", "%20"));
                httpPost.setEntity(new StringEntity(data, "UTF-8"));
                base = httpPost;
            } else if ("DELETE".equalsIgnoreCase(method)) {
                HttpDelete httpDelete = new HttpDelete(getString(sUrl).replace(" ", "%20"));
                base = httpDelete;
            } else {
                HttpGet httpGet = new HttpGet(getString(sUrl).replace(" ", "%20"));
                base = httpGet;
            }

            //          base.setHeader("Content-Type", "UTF-8");
            base.setHeader("Content-Type", "application/json");

            BasicHttpContext localContext = new BasicHttpContext();
            CookieStore cookieStore = new BasicCookieStore();
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            HttpResponse httpResponse = HTTP_CLIENT.execute(base, localContext);

            Map<String, List<String>> headerFields = Maps.newHashMap();

            Header[] headers = httpResponse.getAllHeaders();

            LOG.debug("Response Headers for " + sUrl);

            for (Header header : headers) {
                LOG.debug(header.getName() + " -> " + header.getValue());

                List<String> headerList = Lists.newArrayList();

                HeaderElement[] headerElements = header.getElements();
                for (HeaderElement headerElement : headerElements) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(headerElement.getName());
                    if (headerElement.getValue() != null) {
                        sb.append("=").append(headerElement.getValue());
                    }

                    if (headerElement.getParameterCount() > 0) {
                        NameValuePair[] parameters = headerElement.getParameters();
                        for (NameValuePair parameter : parameters) {
                            sb.append("; ").append(parameter.getName()).append("=").append(parameter.getValue());
                        }
                    }

                    System.out.println("-- \n " + sb.toString());
                    headerList.add(sb.toString());
                }
                headerFields.put(header.getName(), headerList);
            }

            HttpEntity httpEntity = httpResponse.getEntity();

            content = getStringFromInputStream(httpEntity.getContent());

            System.out.println(" " + httpEntity.getContent().toString());
        } catch (Exception e) {
            LOG.error("getContent: " + sUrl, e);
        }

        LOG.debug("Fetched content for url: " + sUrl);
        //        LOG.debug("Content Length: " + content.length());
        return content;
    }

    /**String
     * @param s 
     * @param defaultValue 
     * @return string utility
     */
    public static String getString(String s, String defaultValue) {

        return isEmpty(s) ? defaultValue : s.trim();
    }

    public static boolean isEmpty(String s) {

        return isNull(s) || s.trim().length() == 0;
    }

    public static <T> T isNull(T o, T defaultValue) {

        return isNull(o) ? defaultValue : o;
    }

    private static <T> boolean isNull(T o) {

        return o == null;
    }

    public static String getString(String s) {

        return getString(s, EMPTY_STRING);
    }

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

}

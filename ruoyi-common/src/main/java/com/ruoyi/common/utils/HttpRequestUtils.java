package com.ruoyi.common.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import javax.net.ssl.*;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpRequestUtils.class); // 日志记录

    /**
     * @param url
     * @param jsonParam
     * @param headers
     * @return
     */
    public static JSONObject httpPost(String url, JSONObject jsonParam, Map<String, String> headers) {
        // post请求返回结果
        DefaultHttpClient httpClient = new DefaultHttpClient();
        JSONObject jsonResult = null;
        HttpPost method = new HttpPost(url);
        try {
            if (null != jsonParam) {
                // 解决中文乱码问题
                StringEntity entity = new StringEntity(AesTool.Encrypt("1234567890123456", jsonParam.toString()),
                        "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json;charset=UTF-8");
                method.setEntity(entity);
            }
            // 添加http headers
            if (headers != null) {
                for (String key : headers.keySet()) {
                    method.addHeader(key, headers.get(key));
                }
            }
            HttpResponse result = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            /** 请求发送成功，并得到响应 **/
            if (result.getStatusLine().getStatusCode() == 200) {
                String str = "";
                try {
                    /** 读取服务器返回过来的json字符串数据 **/
                    str = EntityUtils.toString(result.getEntity(), "UTF-8");
                    /** 把json字符串转换成json对象 **/
                    jsonResult = JSONObject.parseObject(str);
                } catch (Exception e) {
                    logger.error("post请求提交失败:" + url, e);
                }
            }
        } catch (IOException e) {
            logger.error("post请求提交失败:" + url, e);
        }
        return jsonResult;
    }

    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public static JSONObject httpGet(String url) {
        // get请求返回结果
        JSONObject jsonResult = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            // 发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            /** 请求发送成功，并得到响应 **/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                HttpEntity entity = response.getEntity();
                /** 读取服务器返回过来的json字符串数据 **/
                byte[] byteArray = EntityUtils.toByteArray(entity);
                String strResult = null;
                if (byteArray != null) {
                    strResult = new String(byteArray, "UTF-8");
                }
                logger.info(url+"出参==>"+strResult);
                /** 把json字符串转换成json对象 **/
                if (strResult.startsWith("{") && strResult.endsWith("}")) {
                    jsonResult = JSONObject.parseObject(strResult);
                } else {
                    return null;
                }
            } else {
                logger.error("get请求提交失败:" + url);
            }
        } catch (IOException e) {
            logger.error("get请求提交失败:" + url, e);
        }
        return jsonResult;
    }

    public static JSONObject httpGetList(String url) {
        // get请求返回结果
        JSONObject jsonResult = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            // 发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            /** 请求发送成功，并得到响应 **/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                HttpEntity entity = response.getEntity();
                /** 读取服务器返回过来的数组数据 **/
                byte[] byteArray = EntityUtils.toByteArray(entity);
                String strResult = null;
                if (byteArray != null) {
                    strResult = new String(byteArray, "UTF-8");
                }
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("result", strResult);
                /** 把json字符串转换成json对象 **/
                jsonResult = JSONObject.parseObject(resultMap.toString());
                url = URLDecoder.decode(url, "UTF-8");
            } else {
                logger.error("get请求提交失败:" + url);
            }
        } catch (IOException e) {
            logger.error("get请求提交失败:" + url, e);
        }
        return jsonResult;
    }

    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public static JSONArray bspHttpGet(String url) {
        // get请求返回结果
        JSONArray jsonResult = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            // 发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            /** 请求发送成功，并得到响应 **/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /** 读取服务器返回过来的json字符串数据 **/
                String strResult = EntityUtils.toString(response.getEntity());
                System.out.println("********************" + strResult);
                /** 把json字符串转换成json对象 **/
                jsonResult = JSONArray.parseArray(strResult);
                url = URLDecoder.decode(url, "UTF-8");
            } else {
                logger.error("get请求提交失败:" + url);
            }
        } catch (IOException e) {
            logger.error("get请求提交失败:" + url, e);
        }
        return jsonResult;
    }

    public static List<Map<String, Object>> HttpGetlist(String url) {
        // get请求返回结果
        JSONArray jsonResult = null;
        List<Map<String, Object>> returnList = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            // 发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            /** 请求发送成功，并得到响应 **/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                String strResult = EntityUtils.toString(response.getEntity());
                System.out.println(strResult.toString());
                strResult = strResult.replace("},{", "~~").replace("{", "").replace("}", "").replace("[", "")
                        .replace("]", "");
                String[] strArr = strResult.split("[~~]");
                returnList = new ArrayList<Map<String, Object>>();
                Map<String, Object> map = null;
                for (String str : strArr) {
                    str = str.replace("/{", "").replace("/}", "");
                    String[] keys = str.split("[,]");
                    map = new HashMap<String, Object>();
                    for (String obj : keys) {
                        if (StringUtils.isNotEmptyOrNull(obj)) {
                            System.out.println("@@@@@@@@" + obj + "#########");
                            String[] objInfo = obj.split("[:]");
                            map.put(objInfo[0], objInfo[1]);
                        } else {
                            continue;
                        }

                    }
                    returnList.add(map);
                }
            } else {
                logger.error("get请求提交失败:" + url);
            }
        } catch (IOException e) {
            logger.error("get请求提交失败:" + url, e);
        }
        return returnList;
    }

    public static String doPostHttps(String url, Map<String, Object> paramMap, String charset) {
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try {
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            // 设置参数
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            Iterator iterator = paramMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Object> elem = (Entry<String, Object>) iterator.next();
                list.add(new BasicNameValuePair(elem.getKey(), elem.getValue().toString()));
            }
            if (list.size() > 0) {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, charset);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {

        String webUrl = "http://www.199it.com/archives/category/service/3g";
        JSONObject jsonObject1 = HttpRequestUtils.httpGet(webUrl);
        JSONArray jsonArray = HttpRequestUtils.bspHttpGet(webUrl);
        JSONObject object = HttpRequestUtils.httpGetList(webUrl);
        List<Map<String, Object>> mapList = HttpRequestUtils.HttpGetlist(webUrl);
        JSONObject jsonObject = HttpRequestUtils.httpGetWithCookie(webUrl, null);
        String get = HttpRequestUtils.httpsRequest(webUrl, "GET", null);

//		Map<String, Object> paramMap = new HashMap<String, Object>();
//		paramMap.put("appid", "20002");
//		paramMap.put("appName", "业支待办");
//		Map<String, Object> param = new HashMap<String, Object>();
//		param.put("username", "liping");
//		param.put("ticket",
//				"5d1961bd1bb8556875a0a461db690382174ef5bf77afe3ad07163f03f39ada013f0417d0ca563780c5fb48f6911529fef3283aefb0436b153340c931415f15390ce805dc050bd4ec");
//		paramMap.put("param", param);
//
//		JSONObject jsonPram = new JSONObject();
//		jsonPram.putAll(paramMap);
//
//		System.out.println(AesTool.Encrypt("1234567890123456", jsonPram.toString()));

//		try {
//			String executePostHttpConn = HttpUtils
//					.executeSSLPost(
//							"http://192.168.50.59:8010/oncon-httpserver/apis/m1_contact_login/v2.0",
//							"{\"version\":\"2.0\",\"id\":320704,\"type\":\"m1_contact_login\",\"action\":\"request\",\"username\":\"008686262152851\",\"password\":\"111111\",\"appid\":\"com.sitech.oncon.cug\",\"timestamp\":\"\"}");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		JSONObject rest = httpPost("http://10.120.26.44:80/services/api/auth/sso", jsonPram, null);
//		System.out.println("");

        // 云公文统一待办登录
        String preRrl = "http://oaapp.chinaunicomglobal.com:3000";

        String url = preRrl + "/portal/cloudUnifiedLogin";
        JSONObject paramMap = new JSONObject();

        url = preRrl + "/portal/assertpurchase/listAssertPurchasePending";
        paramMap.clear();
        paramMap.put("auditorCode", "shirleyzhao");
        paramMap.put("code", "");
        paramMap.put("title", "");
        paramMap.put("flowstatus", 1);
        paramMap.put("startIndex", 0);
        paramMap.put("limit", 10);
        paramMap.put("token", "495c472f0d6f7ef7b41a2bf8d1fca7b784a5fee880df725b64fa366d4c55a9acc315253a77db25dd");

//		Map paramMap = new HashMap();
//		paramMap.put("pwd", "hejj_cug3...");
//		paramMap.put("user", "hejj59");
//		paramMap.put("contractcookie", globleArr);
//		System.out.println("云公文统一待办登录-入参"+paramMap.toString());
//		String cloudUnifiedLogin = doPostHttps(url, paramMap, "UTF-8");
//		Map<String, String> header = new HashMap<String, String>();
//		header.put("Accept", "application/json, text/plain, */*");
//		header.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
//		JSONObject httpPost = httpPost(url, paramMap, header);
        String httpPostWithJson = httpPostWithJsonBody(url, paramMap);
        System.out.println("云公文统一待办登录-出参" + httpPostWithJson);

    }

    public static String executeSSLGet(String url) throws Exception {

        HttpsURLConnection connection = null;
        String response = "";
        try {

            connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);

            int code = connection.getResponseCode();
            logger.info("http code : {}", code);

            if (200 == code) {
                InputStream in = connection.getInputStream();
                StringBuffer out = new StringBuffer();
                byte[] b = new byte[4096];
                for (int n; (n = in.read(b)) != -1; ) {
                    out.append(new String(b, 0, n, "UTF-8"));
                }
                response = out.toString();
            }

        } catch (Exception e) {
            logger.error("http请求异常", e);
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        logger.info("响应报文：{}", response);

        return response;
    }

    public static String executeSSLPost(String url, String request) throws Exception {
        url = url.trim();
        logger.info("请求报文：\n{}", request);
        HttpURLConnection connection = null;
        String response = "";
        try {

            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);

            connection.getOutputStream().write(request.getBytes("UTF-8"));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            int code = connection.getResponseCode();
            logger.info("http code : {}", code);

            if (200 == code) {

                InputStreamReader ins = new InputStreamReader(connection.getInputStream(), "UTF-8");

                StringWriter output = new StringWriter();
                int n = 0;
                char[] buffer = new char[1024 * 4];
                while (-1 != (n = ins.read(buffer))) {
                    output.write(buffer, 0, n);
                }
                response = output.toString();
            }

        } catch (Exception e) {
            logger.error("http请求异常", e);
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        logger.info("响应报文：{}", response);

        return response;
    }

    public static String executeSSLPostInputStream(String url, InputStream in) throws Exception {
        logger.info("流文件ssl post提交URL=\n{}", url);

        HttpURLConnection connection = null;
        String response = "";
        try {

            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            byte[] bs = new byte[1024];
            while (in.read(bs) > 0) {
                connection.getOutputStream().write(bs);
            }
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            int code = connection.getResponseCode();
            logger.info("http code : {}", code);

            if (200 == code) {

                InputStreamReader ins = new InputStreamReader(connection.getInputStream(), "UTF-8");

                StringWriter output = new StringWriter();
                int n = 0;
                char[] buffer = new char[1024 * 4];
                while (-1 != (n = ins.read(buffer))) {
                    output.write(buffer, 0, n);
                }
                response = output.toString();
            }

        } catch (Exception e) {
            logger.error("http请求异常", e);
            throw e;
        } finally {
            if (connection != null) {
                in.close();
                connection.disconnect();
            }
        }

        logger.info("响应报文：{}", response);

        return response;
    }

    private static class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

    private static class MyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    static {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
        } catch (Exception e) {

        }
    }

    /*
     * 处理https GET/POST请求 请求地址、请求方法、参数
     */
    public static String httpsRequest(String requestUrl, String requestMethod, String outputStr) {
        StringBuffer buffer = null;
        try {
            // 创建SSLContext
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManager[] tm = {new MyTrustManager()};
            // 初始化
            sslContext.init(null, tm, new SecureRandom());
            ;
            // 获取SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL url = new URL(requestUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(requestMethod);
            // 设置当前实例使用的SSLSoctetFactory
            conn.setSSLSocketFactory(ssf);
            conn.connect();
            // 往服务器端写内容
            if (null != outputStr) {
                OutputStream os = conn.getOutputStream();
                os.write(outputStr.getBytes("utf-8"));
                os.close();
            }
            // 读取服务器端返回的内容
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            buffer = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    /**
     * json格式參數 http及https请求
     *
     * @param jsonObj
     * @param url
     * @param appId
     * @return
     */
    public static String httpPostWithJson(JSONObject jsonObj, String url, String appId) {
        String result = null;
        HttpPost post = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();

            // 设置超时时间
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 120000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 120000);

            post = new HttpPost(url);
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            post.setHeader("Connection", "Close");
            String sessionId = getSessionId();
            post.setHeader("SessionId", sessionId);
            post.setHeader("appid", appId);

            // 构建消息实体
            StringEntity entity = new StringEntity(jsonObj.toString(), Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);
            if (response != null) {
                // 检验返回码
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        result = EntityUtils.toString(resEntity, "UTF-8");
                    }
                    int retCode = 0;
                    String sessendId = "";
                    // 返回码中包含retCode及会话Id
                    for (Header header : response.getAllHeaders()) {
                        if (header.getName().equals("retcode")) {
                            retCode = Integer.parseInt(header.getValue());
                        }
                        if (header.getName().equals("SessionId")) {
                            sessendId = header.getValue();
                        }
                    }
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        } finally {
            if (post != null) {
                try {
                    post.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new ServiceException(e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * json格式參數 http及https请求
     *
     * @param jsonObj
     * @param url
     * @param appId
     * @return
     */
    public static String httpPostWithJsonForSmartPortal(Object jsonObj, String url, String appId) {
        String result = null;
        HttpPost post = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();

            // 设置超时时间
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);

            post = new HttpPost(url);
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            post.setHeader("Connection", "Close");
            String sessionId = getSessionId();
            post.setHeader("SessionId", sessionId);
            post.setHeader("appid", appId);

            // 构建消息实体
            StringEntity entity = new StringEntity(jsonObj.toString(), Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);
            if (response != null) {
                // 检验返回码
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        result = EntityUtils.toString(resEntity, "UTF-8");
                    }
                    int retCode = 0;
                    String sessendId = "";
                    // 返回码中包含retCode及会话Id
                    for (Header header : response.getAllHeaders()) {
                        if (header.getName().equals("retcode")) {
                            retCode = Integer.parseInt(header.getValue());
                        }
                        if (header.getName().equals("SessionId")) {
                            sessendId = header.getValue();
                        }
                    }
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * json格式參數 http及https请求
     *
     * @param jSONArray
     * @param url
     * @param appId
     * @return
     */
    public static String httpPostWithJSONArray(JSONArray jSONArray, String url, String appId) {
        String result = null;
        HttpPost post = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();

            // 设置超时时间
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);

            post = new HttpPost(url);
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            post.setHeader("Connection", "Close");
            String sessionId = getSessionId();
            post.setHeader("SessionId", sessionId);
            post.setHeader("appid", appId);

            // 构建消息实体
            StringEntity entity = new StringEntity(jSONArray.toString(), Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);
            if (response != null) {
                // 检验返回码
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        result = EntityUtils.toString(resEntity, "UTF-8");
                    }
                    int retCode = 0;
                    String sessendId = "";
                    // 返回码中包含retCode及会话Id
                    for (Header header : response.getAllHeaders()) {
                        if (header.getName().equals("retcode")) {
                            retCode = Integer.parseInt(header.getValue());
                        }
                        if (header.getName().equals("SessionId")) {
                            sessendId = header.getValue();
                        }
                    }
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        } finally {
            if (post != null) {
                try {
                    post.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new ServiceException(e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * json格式參數 http及https请求
     * 工作平台接口处理
     * @param jsonObj
     * @param url
     * @param userCode
     * @return
     */
    public static String httpPostWithJsonForOA(JSONObject jsonObj, String url, String userCode) {
        String result = null;
        HttpPost post = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();

            // 设置超时时间
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 1000000);

            post = new HttpPost(url);
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            post.setHeader("Connection", "Close");
            String sessionId = getSessionId();
            post.setHeader("SessionId", sessionId);
            post.setHeader("userCode", userCode);

            // 构建消息实体
            StringEntity entity = new StringEntity(jsonObj.toString(), Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);
            if (response != null) {
                // 检验返回码
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        result = EntityUtils.toString(resEntity, "UTF-8");
                    }
                    int retCode = 0;
                    String sessendId = "";
                    // 返回码中包含retCode及会话Id
                    for (Header header : response.getAllHeaders()) {
                        if (header.getName().equals("retcode")) {
                            retCode = Integer.parseInt(header.getValue());
                        }
                        if (header.getName().equals("SessionId")) {
                            sessendId = header.getValue();
                        }
                    }
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        } finally {
            if (post != null) {
                try {
                    post.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new ServiceException(e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * json格式參數 http及https请求
     * 4A系统接口调用处理
     * @param jsonObj
     * @param url
     * @param headerMap
     * @return
     */
    public static String httpPostWithJsonForHeader(JSONObject jsonObj, String url, Map<String,String> headerMap) {
        String result = null;
        HttpPost post = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();

            // 设置超时时间
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 1000000);

            post = new HttpPost(url);
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            post.setHeader("Connection", "Close");

            //遍历headerMap, 填装请求头
            for(Entry<String, String> entry : headerMap.entrySet()){
                String mapKey = entry.getKey();
                String mapValue = entry.getValue();
                post.setHeader(mapKey, mapValue);
            }

            // 构建消息实体
            StringEntity entity = new StringEntity(jsonObj.toString(), Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);
            if (response != null) {
                // 检验返回码
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        result = EntityUtils.toString(resEntity, "UTF-8");
                    }
                    int retCode = 0;
                    String sessendId = "";
                    // 返回码中包含retCode及会话Id
                    for (Header header : response.getAllHeaders()) {
                        if (header.getName().equals("retcode")) {
                            retCode = Integer.parseInt(header.getValue());
                        }
                        if (header.getName().equals("SessionId")) {
                            sessendId = header.getValue();
                        }
                    }
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        } finally {
            if (post != null) {
                try {
                    post.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new ServiceException(e.getMessage());
                }
            }
        }
        return result;
    }

    // 构建唯一会话Id
    public static String getSessionId() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        return str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23)
                + str.substring(24);
    }

    public static JSONObject httpGetWithCookie(String url, CookieStore cookieStore) {
        // get请求返回结果
        JSONObject jsonResult = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            client.setCookieStore(cookieStore);
            // 发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            /** 请求发送成功，并得到响应 **/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                HttpEntity entity = response.getEntity();
                /** 读取服务器返回过来的json字符串数据 **/
                String strResult = EntityUtils.toString(response.getEntity());
                /** 把json字符串转换成json对象 **/
                jsonResult = JSONObject.parseObject(strResult);
                url = URLDecoder.decode(url, "UTF-8");
            } else {
                logger.error("get请求提交失败:" + url);
            }
        } catch (IOException e) {
            logger.error("get请求提交失败:" + url, e);
        }
        return jsonResult;
    }

    /**
     * 方法功能说明： post携带cookie请求 创建时间：2019年6月25日 开发者：hejj59@chinaunicom.cn @参数： @param
     * url @参数： @param paramMap @参数： @return @return String @throws
     */
    public static JSONObject httpPostWithCookie(String url, JSONObject jsonParam, CookieStore cookieStore) {
        // post请求返回结果
        DefaultHttpClient httpClient = new DefaultHttpClient();
        JSONObject jsonResult = null;
        HttpPost method = new HttpPost(url);
        try {
            if (null != jsonParam) {
                // 解决中文乱码问题
                StringEntity entity = new StringEntity(jsonParam.toString(), Charset.forName("UTF-8"));
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json;charset=UTF-8");
                method.setEntity(entity);
            }
            httpClient.setCookieStore(cookieStore);
            HttpResponse result = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            /** 请求发送成功，并得到响应 **/
            if (result.getStatusLine().getStatusCode() == 200) {
                String str = "";
                try {
                    /** 读取服务器返回过来的json字符串数据 **/
                    str = EntityUtils.toString(result.getEntity(), "UTF-8");
                    /** 把json字符串转换成json对象 **/
                    jsonResult = JSONObject.parseObject(str);
                } catch (Exception e) {
                    logger.error("post请求提交失败:" + url, e);
                }
            }
        } catch (IOException e) {
            logger.error("post请求提交失败:" + url, e);
        }
        return jsonResult;
    }

    public static String httpPostWithJsonBody(String url, JSONObject paramMap) {

        OutputStreamWriter out = null;
        InputStream is = null;
        try {
            URL requrl = new URL(url);// 创建连接
            HttpURLConnection connection = (HttpURLConnection) requrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("POST"); // 设置请求方式
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            connection.connect();
            out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
            out.append(paramMap.toString());

            out.flush();
            out.close();

            // 读取响应
            is = connection.getInputStream();
            int length = (int) connection.getContentLength();// 获取长度
            if (length != -1) {
                byte[] data = new byte[length];
                byte[] temp = new byte[512];
                int readLen = 0;
                int destPos = 0;
                while ((readLen = is.read(temp)) > 0) {
                    System.arraycopy(temp, 0, data, destPos, readLen);
                    destPos += readLen;
                }
                String result = new String(data, "UTF-8"); // utf-8编码
//                System.out.println("主机返回:" + result);
                return result;
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String httptPostWithXml(String url, String method, List<Object> xmlParamList) {
        try {
            // 第一步：创建服务地址
            URL requestUrl = new URL(url);
            // 第二步：打开一个通向服务地址的连接
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            // 第三步：设置参数
            // 3.1发送方式设置：POST必须大写
            connection.setRequestMethod("POST");
            // 3.2设置数据格式：content-type
            connection.setRequestProperty("content-type", "text/xml;charset=utf-8");
            // 3.3设置输入输出，因为默认新创建的connection没有读写权限，
            connection.setDoInput(true);
            connection.setDoOutput(true);
            // 第四步：组织SOAP数据，发送请求
            String soapXML = getXML(xmlParamList, method);
            // 将信息以流的方式发送出去
            OutputStream os = connection.getOutputStream();
            os.write(soapXML.getBytes());
            // 第五步：接收服务端响应，打印
            int responseCode = connection.getResponseCode();
            String returnStr = "";
            if (200 == responseCode) {// 表示服务端响应成功
                // 获取当前连接请求返回的数据流
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String temp = null;
                while (null != (temp = br.readLine())) {
                    sb.append(temp);
                }
                /**
                 * 打印结果
                 */
                System.out.println(sb.toString());
                returnStr = sb.toString();
                is.close();
                isr.close();
                br.close();
            }
            os.close();
            return returnStr;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public static String getXML(List<Object> xmlParamList, String method) {
        String soapXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:pm=\"http://impl.webservice.platform.unissoft.com/\">" + "<soapenv:Body>" + "<pm:" + method
                + ">";
        for (int i = 0; i < xmlParamList.size(); i++) {
            soapXML = soapXML + "<arg" + i + ">" + xmlParamList.get(i) + "</arg" + i + ">";
        }
        soapXML = soapXML + "</pm:" + method + ">" + "</soapenv:Body>" + "</soapenv:Envelope>";
        return soapXML;
    }

    /**
     * @param requestUrl 微信上传临时素材的接口url
     * @param file       要上传的文件
     * @return String 上传成功后，微信服务器返回的消息
     * @desc ：微信上传素材的请求方法
     */
    public static String httpRequest(String requestUrl, File file) {

        StringBuffer buffer = new StringBuffer();
        try {
            // 1.建立连接
            URL url = new URL(requestUrl);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection(); // 打开链接

            // 1.1输入输出设置
            httpUrlConn.setDoInput(true);
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setUseCaches(false); // post方式不能使用缓存
            // 1.2设置请求头信息
            httpUrlConn.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConn.setRequestProperty("Charset", "UTF-8");
            // 1.3设置边界
            String BOUNDARY = "----------" + System.currentTimeMillis();
            httpUrlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

            // 请求正文信息
            // 第一部分：
            // 2.将文件头输出到微信服务器
            StringBuilder sb = new StringBuilder();
            sb.append("--"); // 必须多两道线
            sb.append(BOUNDARY);
            sb.append("\r\n");
            sb.append("Content-Disposition: form-data;name=\"media\";filelength=\"" + file.length() + "\";filename=\""
                    + file.getName() + "\"\r\n");
            sb.append("Content-Type:application/octet-stream\r\n\r\n");
            byte[] head = sb.toString().getBytes("utf-8");
            // 获得输出流
            OutputStream outputStream = new DataOutputStream(httpUrlConn.getOutputStream());
            // 将表头写入输出流中：输出表头
            outputStream.write(head);

            // 3.将文件正文部分输出到微信服务器
            // 把文件以流文件的方式 写入到微信服务器中
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            int bytes = 0;
            byte[] bufferOut = new byte[1024];
            while ((bytes = in.read(bufferOut)) != -1) {
                outputStream.write(bufferOut, 0, bytes);
            }
            in.close();
            // 4.将结尾部分输出到微信服务器
            byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线
            outputStream.write(foot);
            outputStream.flush();
            outputStream.close();

            // 5.将微信服务器返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
        } catch (IOException e) {
            System.out.println("发送POST请求出现异常！" + e);
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public static String doGet(String url) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String result = "";
        try {
            // 通过址默认配置创建一个httpClient实例
            httpClient = HttpClients.createDefault();
            // 创建httpGet远程连接实例
            HttpGet httpGet = new HttpGet(url);
            // 设置请求头信息，鉴权
            httpGet.setHeader("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
            // 设置配置请求参数
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000)// 连接主机服务超时时间
                    .setConnectionRequestTimeout(35000)// 请求超时时间
                    .setSocketTimeout(60000)// 数据读取超时时间
                    .build();
            // 为httpGet实例设置配置
            httpGet.setConfig(requestConfig);
            // 执行get请求得到返回对象
            response = httpClient.execute(httpGet);
            // 通过返回对象获取返回数据
            HttpEntity entity = response.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            result = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String doPost(String url, Map<String, Object> paramMap) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        String result = "";
        // 创建httpClient实例
        httpClient = HttpClients.createDefault();
        // 创建httpPost远程连接实例
        HttpPost httpPost = new HttpPost(url);
        // 配置请求参数实例
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000)// 设置连接主机服务超时时间
                .setConnectionRequestTimeout(35000)// 设置连接请求超时时间
                .setSocketTimeout(60000)// 设置读取数据连接超时时间
                .build();
        // 为httpPost实例设置配置
        httpPost.setConfig(requestConfig);
        // 设置请求头
        httpPost.addHeader("Content-Type", "application/json");
        // 封装post请求参数
        if (null != paramMap && paramMap.size() > 0) {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            // 通过map集成entrySet方法获取entity
            Set<Entry<String, Object>> entrySet = paramMap.entrySet();
            // 循环遍历，获取迭代器
            Iterator<Entry<String, Object>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Entry<String, Object> mapEntry = iterator.next();
                nvps.add(new BasicNameValuePair(mapEntry.getKey(), mapEntry.getValue().toString()));
            }

            // 为httpPost设置封装好的请求参数
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            // httpClient对象执行post请求,并返回响应参数对象
            httpResponse = httpClient.execute(httpPost);
            // 从响应对象中获取响应内容
            HttpEntity entity = httpResponse.getEntity();
            result = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


    /**
     * http Post请求，兼容参数超长
     * @param urlStr 请求地址
     * @param postData 请求json报文
     * @return Map<String, Object>
     *
     */
    public static Map<String, Object> httpPostWithJson(String urlStr, String postData) throws Exception {
        Map returnMap = new HashMap();
        byte[] postDataBytes = postData.getBytes("UTF-8");

        //开始访问
        HttpURLConnection conn = (HttpURLConnection)(new URL(urlStr)).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setConnectTimeout(35000);
        conn.setReadTimeout(60000);
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) >= 0;) {
            sb.append((char)c);
        }
        in.close();
        conn.disconnect();

        String responseStr = sb.toString();
        logger.info("httpPostWithJson - responseStr <== " + responseStr);
        if (StringUtils.isEmpty(responseStr)) {
            responseStr = "{}";
        }
        int statusCode = conn.getResponseCode();
        logger.info("httpPostWithJson - statusCode <== " + statusCode);
        if (HttpServletResponse.SC_OK == statusCode) {
            JSONObject dataJson = (JSONObject) JSONObject.parse(responseStr);
            returnMap = new HashMap(dataJson);
        }

        return returnMap;
    }
}
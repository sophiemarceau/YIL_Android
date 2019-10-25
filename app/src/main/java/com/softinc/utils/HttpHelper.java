package com.softinc.utils;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 6/29/2015.
 */
public class HttpHelper {
    public static String HttpGetRequest(String url, List<BasicNameValuePair> params) {
        String result = "";

        String param = URLEncodedUtils.format(params, "UTF-8");

        HttpGet getMethod = new HttpGet(url + "?" + param);
        HttpClient httpClient = new DefaultHttpClient();

        try {
            HttpResponse response = httpClient.execute(getMethod); //发起GET请求
            result = EntityUtils.toString(response.getEntity());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String HttpPostRequest(String url, List<BasicNameValuePair> params) {
        String result = "";
        try {
            HttpPost postMethod = new HttpPost(url);
            HttpClient httpClient = new DefaultHttpClient();
            postMethod.setEntity(new UrlEncodedFormEntity(params, "utf-8")); //将参数填入POST Entity中

            HttpResponse response = httpClient.execute(postMethod); //执行POST方法
            result = EntityUtils.toString(response.getEntity()); //获取响应内容

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}

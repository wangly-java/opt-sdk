package com.ai.opt.sdk.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class HttpClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    /**
     * 发送Post请求
     * @param url 请求的url地址
     * @param param 参数报文体
     * @param header 请求头信息
     * @return 处理结果报文
     * @throws IOException
     * @throws URISyntaxException
     * @author gucl
     */
    public static String sendPost(String url, String param, Map<String, String> header) throws IOException,
            URISyntaxException {
    	logger.info("restful request url:"+url);
    	logger.info("restful request param:"+param);
    	StringBuffer buffer = new StringBuffer();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(new URL(url).toURI());
        if(header!=null){
        	for (Map.Entry<String, String> entry : header.entrySet()) {
        		httpPost.setHeader(entry.getKey(), entry.getValue());
        	}        	
        }
        StringEntity dataEntity = new StringEntity(param, ContentType.APPLICATION_JSON);
        httpPost.setEntity(dataEntity);
        CloseableHttpResponse response = httpclient.execute(httpPost);
        try {
        	//请求成功且有返回体
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        entity.getContent()));
                String tempStr;
                while ((tempStr = reader.readLine()) != null)
                    buffer.append(tempStr);
                logger.info("=============http请求成功，返回结果===="+buffer.toString()); 
                return buffer.toString();
            } 
            //请求成功，但没有返回体
            else if (response.getStatusLine().getStatusCode() == 204) {
            	RestResponse resp204 = new RestResponse();
            	resp204.setResponseCode(String.valueOf(response.getStatusLine().getStatusCode()));
            	resp204.setResponseMessage("请求成功，无返回体！");
            	String resp204Json = JSON.toJSONString(resp204);
            	logger.info("=============http请求成功，无返回体===="+resp204Json.toString());
            	
            	return resp204Json.toString();
            }
            //请求失败
            else {
            	RestResponse respError = new RestResponse();
            	respError.setResponseCode(String.valueOf(response.getStatusLine().getStatusCode()));
            	respError.setResponseMessage("请求异常！");
            	String respErrorJson = JSON.toJSONString(respError);
            	logger.error("=============http请求异常===="+respErrorJson.toString());
            	
            	return respErrorJson.toString();
            }
        }
        //系统异常
        catch(Exception e){
        	logger.error(e.getMessage(),e);
        	RestResponse sysError = new RestResponse();
        	sysError.setResponseCode("DUBBO_REST_SYSTEM_ERROR");
        	sysError.setResponseMessage(e.getMessage());
        	String sysErrorJson = JSON.toJSONString(sysError);
        	logger.error("=============HttpPost请求系统异常===="+sysErrorJson.toString(),e);
        	
        	return sysErrorJson.toString();
        }
        //释放资源
        finally {
        	try {
        		if(response != null ){
        			response.close();    			
        		}
        		if(httpclient != null ){
        			httpclient.close();    			
        		}
        	} catch (IOException e) {
	        	logger.error(e.getMessage(),e);
			}
        }
        
        
    }

    /**
     * 发送Post请求
     * @param url
     * @param param
     * @return
     * @author gucl
     */
    public static String sendPost(String url, String param) {
        logger.info("restful address : " + url);
        logger.info("param : " + param);
        String result = "";
        try {
            result = HttpClientUtil.sendPost(url, param, null);
            logger.info("result : " + result);
        } catch (IOException e) {
            String errorMessage = e.getMessage();
            logger.error(errorMessage, e);
        } catch (URISyntaxException e) {
            String errorMessage = e.getMessage();
            logger.error(errorMessage, e);
        }
        // 请求发生异常后，result 为 空
        return result;
    }

    /**
     * 发送GET请求
     * 
     * @param url
     *            目的地址
     * @param parameters
     *            请求参数，Map类型。
     * @return 远程响应结果
     */
    public static String sendGet(String url, Map<String, String> parameters) {
    	return sendGet(url,parameters,null);
    }
    
    public static String sendGet(String url, Map<String, String> parameters, Map<String, String> header) {
        StringBuffer buffer = new StringBuffer();// 返回的结果
        BufferedReader in = null;// 读取响应输入流
        StringBuffer sb = new StringBuffer();// 存储参数
        String params = "";// 编码之后的参数
        String full_url = url;
        try {
        	if(parameters!=null){
        		// 编码请求参数
        		if (parameters.size() == 1) {
        			for (String name : parameters.keySet()) {
        				sb.append(name).append("=")
        				.append(java.net.URLEncoder.encode(parameters.get(name), "UTF-8"));
        			}
        			params = sb.toString();
        		} else {
        			for (String name : parameters.keySet()) {
        				sb.append(name).append("=")
        				.append(java.net.URLEncoder.encode(parameters.get(name), "UTF-8"))
        				.append("&");
        			}
        			String temp_params = sb.toString();
        			params = temp_params.substring(0, temp_params.length() - 1);
        		}
        		if(!url.startsWith("?")){
        			full_url += "?" + params;        			
        		}
        		else{
        			full_url += "&" + params;    
        		}
        	}
            logger.info("restful address : " + full_url);
            // 创建URL对象
            URL connURL = new URL(full_url);
            
            // 打开URL连接
            HttpURLConnection httpConn = (HttpURLConnection) connURL
                    .openConnection();
            if(header!=null){
            	for (Map.Entry<String, String> entry : header.entrySet()) {
            		httpConn.addRequestProperty(entry.getKey(), entry.getValue());
            	}            	
            }
            // 建立实际的连接
            httpConn.connect();
            // 定义BufferedReader输入流来读取URL的响应,并设置编码方式
            in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
            String line;
            // 读取返回的内容
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception e) {
        	logger.error(e.getMessage(),e);
        	RestResponse sysError = new RestResponse();
        	sysError.setResponseCode("DUBBO_REST_SYSTEM_ERROR");
        	sysError.setResponseMessage(e.getMessage());
        	String sysErrorJson = JSON.toJSONString(sysError);
        	logger.error("=============HttpGet请求系统异常===="+sysErrorJson.toString(),e);
        	
        	return sysErrorJson.toString();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return buffer.toString();
    }
    
    static class RestResponse implements Serializable {

        private static final long serialVersionUID = 1L;

        private String responseCode;

        private String responseMessage;

        private Object data;

        public RestResponse() {
    		super();
    	}

    	public RestResponse(String responseCode, String responseMessage, Object data) {
            this.responseCode = responseCode;
            this.responseMessage = responseMessage;
            this.data = data;
        }

        public RestResponse(String responseCode, String responseMessage) {
            this.responseCode = responseCode;
            this.responseMessage = responseMessage;
        }

		public String getResponseCode() {
			return responseCode;
		}

		public void setResponseCode(String responseCode) {
			this.responseCode = responseCode;
		}

		public String getResponseMessage() {
			return responseMessage;
		}

		public void setResponseMessage(String responseMessage) {
			this.responseMessage = responseMessage;
		}

		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}

        

    }

    

    /*public static void main(String[] args) throws IOException, URISyntaxException {

//        String result = HttpClientUtil.sendPost(
//                "http://10.1.228.222:15101/serviceAgent/rest/ipaas/dubbo-testA/dubbo-test/testServiceMethod", "{\"count\":1,\"SrcSysCode\":\"1005\"}");
//        System.out.println("++++++++++++  " + result);

        Map<String, String> headerValue = new HashMap<String, String>();
        headerValue.put("appkey","03379980ba661ad9ba678d386e39c1ca");
        headerValue.put("sign","12345");

        String result = HttpClientUtil.sendPost(
                "http://10.1.235.246:8081/serviceAgent/http/BIS-3A-USERADD", "{\"loginname\":\"xj109\",\"orgcode\":\"4001\",\"password\":\"abcdEFG123\",\"status\":\"active\"}", headerValue);
        System.out.println("++++++++++++  " + result);

//        Map<String, String> headerValue = new HashMap<String, String>();
//        headerValue.put("appkey","893f09f81402f23bf5b2bd5596d668b0");
//        headerValue.put("sign","12346");
//
//        String result = HttpClientUtil.sendPost(
//                "http://10.1.235.246:8081/serviceAgent/http/RUNNER-QUERYCUSTIDBYPHONENUM-001",
//                "{\"tenantId\":\"HX\",\"custPhoneNum\":\"15930008252\"}", headerValue);
//        System.out.println("++++++++++++  " + result);


    }*/
}

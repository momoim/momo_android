
package cn.com.nd.momo.dynamic;

import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import cn.com.nd.momo.api.http.HttpToolkit;
import cn.com.nd.momo.api.util.Log;

public class AbsSdk {
    public AbsSdk() {
    }

    public static final String TAG = "Sdk";

    public static class SdkResult {
        public int ret = 0;

        public Object object = 0;

        public String response = "";

        public String Log() {
            return "ret:" + ret + " response:" + response;
        }
    }

    static class HttpWrap {
        public HttpWrap(int md) {
            method = md;
        }

        public final static int HTTP_GET = 0;

        public final static int HTTP_POST = 1;

        public final static int HTTP_PUT = 2;

        public final static int HTTP_DELETE = 3;

        private String function;

        private Map<String, String> jsonParamMap = null;

        private Map<String, String> urlParamMap = null;

        private int method;

        public void setJsonParamMap(Map<String, String> paramMap) {
            jsonParamMap = paramMap;
        }

        public void setUrlParamMap(Map<String, String> paramMap) {
            urlParamMap = paramMap;
        }

        public void setFunction(String fc) {
            function = fc;
        }

        public SdkResult doHttpMethod() {
            String functionWithParam = function;
            // set url param
            if (urlParamMap != null) {
                Iterator<?> iter = urlParamMap.entrySet().iterator();
                while (iter.hasNext()) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<String, String> entry = (Map.Entry<String, String>)iter.next();
                    String key = entry.getKey();
                    String val = entry.getValue();
                    String flag = functionWithParam.contains("?") ? "&" : "?";
                    functionWithParam += flag + (key + "=" + val);
                }
            }

            HttpToolkit http = new HttpToolkit(functionWithParam);
            JSONObject param = new JSONObject();
            SdkResult result = new SdkResult();

            // set json param
            if (jsonParamMap != null) {
                try {
                    Iterator<?> iter = jsonParamMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        @SuppressWarnings("unchecked")
                        Map.Entry<String, String> entry = (Map.Entry<String, String>)iter.next();
                        String key = entry.getKey();
                        String val = entry.getValue();

                        param.put(key, val);
                    }
                } catch (Exception e) {
                    Log.i(TAG, e.toString());
                }
            }

            String strMethod = "";
            switch (method) {
                case HTTP_GET:
                    strMethod = "GET";
                    result.ret = http.DoGet();
                    break;
                case HTTP_POST:
                    strMethod = "POST";
                    result.ret = http.DoPost(param);
                    break;
                case HTTP_DELETE:
                    strMethod = "DELETE";
                    result.ret = http.DoDelete();
                    break;
            // case HTTP_PUT:
            // strMethod = "PUT";
            // result.ret = http.DoPut(param);
            // break;
            }

            Log.i(TAG, "----------------------------------------------------");
            Log.i(TAG, "method : " + strMethod + "\nfunction : " + http.getUrl() + "\nparam :"
                    + param.toString());
            result.response = http.GetResponse();
            Log.i(TAG, "ret" + result.ret + "response" + result.response);
            return result;
        }
    }
}

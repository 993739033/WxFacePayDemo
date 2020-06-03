package com.mainback.wxfacepaydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mainback.wxfacepaydemo.FacePayConfig.JsonType;
import static com.mainback.wxfacepaydemo.FacePayConfig.SecretKey;
import static com.mainback.wxfacepaydemo.FacePayConfig.XmlType;
import static com.mainback.wxfacepaydemo.FacePayUtil.MapToXML;
import static com.mainback.wxfacepaydemo.FacePayUtil.getDeviceSN;
import static com.mainback.wxfacepaydemo.FacePayUtil.getUnixStamp;
import static com.mainback.wxfacepaydemo.FacePayUtil.orderMapByASCLL;
import static com.mainback.wxfacepaydemo.FacePayUtil.sign;

public class MainActivity extends AppCompatActivity {
    private TextView tv_content;
    private static String tag = "wx>>";
    private static String rawdata = "";
    private String auth_info = "";//调用凭证。获取方式参见: get_wxpayface_authinfo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_content = findViewById(R.id.tv_content);
    }

    public void btnInitOnClick(View view) {
        WxPayFace.getInstance().initWxpayface(this, new IWxPayfaceCallback() {
            @Override
            public void response(Map info) throws RemoteException {
                Log.e(tag, info.toString());
                tv_content.setText(tv_content.getText().toString() + "\n" + "init成功" + "\n" + info.toString());

                if (info == null) {
//                    new RuntimeException("调用返回为空").printStackTrace();
                    return;
                }
                String code = (String) info.get("return_code");
                String msg = (String) info.get("return_msg");
                Log.d(tag, "response info :: " + code + " | " + msg);
                /*return_code
                 * SUCCESS	接口成功	无
                 * ERROR	接口失败	展示错误原因（该请求无法通过重试解决）
                 * PARAM_ERROR	参数错误	参照错误提示
                 * SYSTEMERROR	接口返回错误	系统异常，可重试该请求
                 */
                if (code == null || !code.equals("SUCCESS")) {
//                    new RuntimeException("调用返回非成功信息: " + msg).printStackTrace();
                    return;
                }
                Log.d(tag, "调用返回成功");
            }
        });

    }

    public void btnFaceIdOnClick(View view) {
        WxPayFace.getInstance().getWxpayfaceRawdata(new IWxPayfaceCallback() {
            @Override
            public void response(Map info) throws RemoteException {
                Log.e(tag, "获取 rawdata成功：response | getWxpayfaceRawdata " + info.get("rawdata"));
                //此处获取成功rawdata成功以后需要保存作为下一步请求的入参
                rawdata = info.get("rawdata").toString();
                Log.e(tag, rawdata);
                tv_content.setText(tv_content.getText().toString() + "\n" + "getWxpayfaceRawdata获取成功" + "\n" + rawdata);

                if (info == null) {
                    new RuntimeException("调用返回为空").printStackTrace();
                    return;
                }
                String code = (String) info.get("return_code");
                String msg = (String) info.get("return_msg");
                Integer errcode = (Integer) info.get("err_code");
                if (code == null || rawdata == null || !code.equals("SUCCESS")) {
//                    new RuntimeException("调用返回非成功信息,return_msg:" + msg + "   ").printStackTrace();
                    return;
                }
            }
        });
    }

    //获取auth info 后才可使用人脸识别
    public void btnGetAuthInfoOnClick(View view) {
        OkHttpClient okHttpClient = new OkHttpClient();
        LinkedHashMap<String, String> data = new LinkedHashMap<>();
        data.put("store_id", "S001");//门店编号， 由商户定义， 各门店唯一。
        data.put("store_name", "吃不起餐厅");//门店名称，由商户定义。（可用于展示）
        data.put("device_id", FacePayUtil.getDeviceSN());//终端设备编号，由商户定义。
        data.put("rawdata", rawdata);//初始化数据。由微信人脸SDK的接口返回。

        //////需商户账号配置开通///////
        data.put("appid", FacePayConfig.APP_ID);//商户号绑定的公众号/小程序 appid
        data.put("mch_id", FacePayConfig.MCH_ID);//服务商的商户号
//        data.put("sub_appid", FacePayConfig.SUB_APP_ID);//子商户绑定的公众号/小程序 appid(服务商模式) 非必须
        data.put("sub_mch_id", FacePayConfig.SUB_MCH_ID);//子商户号(服务商模式) 非必须
        ///////////////////////////////
        data.put("now", getUnixStamp());//取当前时间，10位unix时间戳。 例如：1239878956
        data.put("version", "1");//版本号。固定为1
        data.put("sign_type", "MD5");//签名类型，目前支持HMAC-SHA256和MD5，默认为MD5
        data.put("nonce_str", FacePayUtil.getRandomString(32));//随机字符串，不长于32位
        data.put("sign", sign(data, SecretKey));//参数签名。详见微信支付签名算法  所有非空value参数+Screctkey MD5加密

        String xml = MapToXML(data);
        RequestBody body = RequestBody.create(XmlType, xml);
        tv_content.setText(tv_content.getText().toString() + "\n" + "xmlData:" + "\n" + xml);
        Log.e(tag, xml);
        Request request = new Request.Builder()
                .url("https://payapp.weixin.qq.com/face/get_wxpayface_authinfo")
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(tag, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Log.d(tag, response.protocol() + " " + response.code() + " " + response.message());
                Headers headers = response.headers();
                for (int i = 0; i < headers.size(); i++) {
                    Log.d(tag, headers.name(i) + ":" + headers.value(i));
                }
                final String content = response.body().string();
                Log.d(tag, "onResponse: " + content);
                Log.d(tag, "return_msg:>>" + FacePayUtil.getXMLValue(content, "return_msg"));
                Log.d(tag, "return_code:>>" + FacePayUtil.getXMLValue(content, "return_code"));
                Log.d(tag, "authinfo:>>" + FacePayUtil.getXMLValue(content, "authinfo"));
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tv_content.setText(tv_content.getText().toString() + "\n" + "Result:>>" + "\n" + content);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void btnGetSnNo(View view) {
        String no = getDeviceSN();
        Log.e(tag, "SN:" + no);
        tv_content.setText(tv_content.getText().toString() + "\n" + "SN号 机器序列号" + "\n" + no);
    }

    public void btnGetFaceCodeOnClick(View view) {
        Map<String, String> m1 = new HashMap<String, String>();
        m1.put("appid", FacePayConfig.APP_ID); // 公众号，必填
        m1.put("mch_id", FacePayConfig.MCH_ID); // 商户号，必填
        m1.put("sub_appid", FacePayConfig.SUB_APP_ID); // 子商户公众账号ID(非服务商模式不填)
        m1.put("sub_mch_id", FacePayConfig.SUB_MCH_ID); // 子商户号(非服务商模式不填)
        m1.put("store_id", "S001"); // 门店编号，必填
//        m1.put("telephone", "用户手机号"); // 用户手机号，用于传递会员手机号到界面输入栏，非必填
//        m1.put("openid", "用户openid"); // 用户openid，用于快捷支付模式，非必填
        m1.put("authinfo", auth_info); // 调用凭证。获取方式参见: get_wxpayface_authinfo 必填
        m1.put("out_trade_no", "S0001121"); // 商户订单号， 必填
        m1.put("total_fee", "1"); // 订单金额（数字），单位：分，必填
        m1.put("face_authtype", "FACEPAY"); // FACEPAY：人脸凭证，常用于人脸支付    FACEPAY_DELAY：延迟支付   必填
        m1.put("ask_face_permit", "1"); // 展开人脸识别授权项，详情见上方接口参数，必填
        m1.put("ask_ret_page", "1"); // 是否展示微信支付成功页，可选值："0"，不展示；"1"，展示，非必填
        WxPayFace.getInstance().getWxpayfaceCode(m1, new IWxPayfaceCallback() {
            @Override
            public void response(final Map info) throws RemoteException {
                if (info == null) {
                    new RuntimeException("调用返回为空").printStackTrace();
                    return;
                }
                String code = (String) info.get("return_code"); // 错误码
                Integer errcode = (Integer) info.get("err_code"); // 二级错误码
                String msg = (String) info.get("return_msg"); // 错误码描述
                String faceCode = info.get("face_code").toString(); // 人脸凭证，用于刷脸支付
                String openid = info.get("openid").toString(); // openid
                String sub_openid = ""; // 子商户号下的openid(服务商模式)
                int telephone_used = 0; // 获取的`face_code`，是否使用了请求参数中的`telephone`
                int underage_state = 0; // 用户年龄信息（需联系微信支付开通权限）
                if (info.get("sub_openid") != null) sub_openid = info.get("sub_openid").toString();
                if (info.get("telephone_used") != null)
                    telephone_used = Integer.parseInt(info.get("telephone_used").toString());
                if (info.get("underage_state") != null)
                    underage_state = Integer.parseInt(info.get("underage_state").toString());
                if (code == null || faceCode == null || openid == null || !code.equals("SUCCESS")) {
                    new RuntimeException("调用返回非成功信息,return_msg:" + msg + "   ").printStackTrace();
                    tv_content.setText(tv_content.getText().toString() + "\n" + "获取失败");
                    return;
                }
                tv_content.setText(tv_content.getText().toString() + "\n" + "faceCode获取成功" + "\n" + faceCode);
            }
        });
    }
}

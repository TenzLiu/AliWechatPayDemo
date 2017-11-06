package com.ltz.aliwechatpaydemo.alipay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alipay.sdk.app.PayTask;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * User: ltz
 * Date: 2017-11-03
 * Time: 15:36
 * Description:: 支付宝支付封装工具类
 */
public class AliPayUtil {
    /** 支付宝支付业务：入参app_id */
    public String APPID = "";

    /** 商户私钥，pkcs8格式 */
    /** 如下私钥，RSA2_PRIVATE 或者 RSA_PRIVATE 只需要填入一个 */
    /** 如果商户两个都设置了，优先使用 RSA2_PRIVATE */
    /** RSA2_PRIVATE 可以保证商户交易在更加安全的环境下进行，建议使用 RSA2_PRIVATE */
    /** 获取 RSA2_PRIVATE，建议使用支付宝提供的公私钥生成工具生成， */
    /** 工具地址：https://doc.open.alipay.com/docs/doc.htm?treeId=291&articleId=106097&docType=1 */
    public String RSA2_PRIVATE = "";
    public String RSA_PRIVATE = "";

    private static final int SDK_PAY_FLAG = 1;

    private WeakReference<Activity> mActivity;
    private OnAlipayListener mListener;

    public void setmListener(OnAlipayListener mListener) {
        this.mListener = mListener;
    }

    public AliPayUtil(Activity activity, String appId, String privateKey) {
        this.mActivity = new WeakReference<>(activity);
        this.APPID = appId;
        this.RSA_PRIVATE = privateKey;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        if (mListener != null) mListener.onSuccess();
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        if (mListener != null) mListener.onCancel();
                    }
                    break;
                }
                default:
                    break;
            }
        };
    };

    /**
     * 支付宝支付业务
     * @param title  标题 不能为空或者“”
     * @param desc 描述 不能为空或者“”
     * @param price 价格 不能为空或者“”
     * @param sn 商品唯一货号 不能为空或者“”
     * @param url 服务器回调url 不能为空或者“”
     */
    public void pay(String title, String desc, String price, String sn, String url) {
        if (TextUtils.isEmpty(APPID) || (TextUtils.isEmpty(RSA2_PRIVATE) && TextUtils.isEmpty(RSA_PRIVATE))) {
            new AlertDialog.Builder(mActivity.get()).setTitle("警告").setMessage("需要配置APPID | RSA_PRIVATE")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                        }
                    }).show();
            return;
        }

        /**
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo的获取必须来自服务端；
         */
        boolean rsa2 = (RSA2_PRIVATE.length() > 0);
        Map<String, String> params = OrderInfoUtil.buildOrderParamMap(APPID, rsa2, title, desc, price, sn, url);
        String orderParam = OrderInfoUtil.buildOrderParam(params);

        String privateKey = rsa2 ? RSA2_PRIVATE : RSA_PRIVATE;
        String sign = OrderInfoUtil.getSign(params, privateKey, rsa2);
        final String orderInfo = orderParam + "&" + sign;

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(mActivity.get());
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.i("msp", result.toString());

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }


    /**
     * get the sdk version. 获取SDK版本号
     *
     */
    public void getSDKVersion() {
        PayTask payTask = new PayTask(mActivity.get());
        String version = payTask.getVersion();
        Log.d("TAG","version:"+version);
    }

    /**
     * 支付回调接口
     *
     * @author lenovo
     *
     */
    public static class OnAlipayListener {
        /**
         * 支付成功
         */
        public void onSuccess() {}

        /**
         * 支付取消
         */
        public void onCancel() {}

        /**
         * 等待确认
         */
        public void onWait() {}
    }
}

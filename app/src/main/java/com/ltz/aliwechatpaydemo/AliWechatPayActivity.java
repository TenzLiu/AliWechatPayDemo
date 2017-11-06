package com.ltz.aliwechatpaydemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ltz.aliwechatpaydemo.alipay.AliPayUtil;
import com.ltz.aliwechatpaydemo.events.Event;
import com.ltz.aliwechatpaydemo.wechatpay.WechatPayUtil;

import de.greenrobot.event.EventBus;

/**
 * User: ltz
 * Date: 2017-11-03
 * Time: 17:17
 * Description::
 */
public class AliWechatPayActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_wechat_pay,tv_ali_pay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        tv_wechat_pay = (TextView) findViewById(R.id.tv_wechat_pay);
        tv_ali_pay = (TextView) findViewById(R.id.tv_ali_pay);
        tv_wechat_pay.setOnClickListener(this);
        tv_ali_pay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_wechat_pay:
                //测试需要签名打包
                WechatPayUtil.WXPayBuilder builder = new WechatPayUtil.WXPayBuilder();
                builder.setAppId("appid")
                        .setPartnerId("partnerid")
                        .setPrepayId("prepayid")
                        .setPackageValue("packageValue")
                        .setNonceStr("nonceStr")
                        .setTimeStamp("timestamp")
                        .setSign("sign")
//                        .build().payNotSign(this,"appid");//支付信息后台接口返回
                        .build().payAndSign(this,"appid","key");//支付信息手机端生成
                break;
            case R.id.tv_ali_pay:
                AliPayUtil alipay = new AliPayUtil(this,"appid","privateKey");
                alipay.setmListener(mAlipayListener);
                alipay.pay("微信支付宝支付测试", "充钱一分钱", "0.01", "orderId", "notify_url");
                break;
        }
    }

    /**
     * 支付宝支付回调
     */
    private AliPayUtil.OnAlipayListener mAlipayListener = new AliPayUtil.OnAlipayListener() {
        @Override
        public void onSuccess() {
            Toast.makeText(AliWechatPayActivity.this,"支付成功",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(AliWechatPayActivity.this,"支付失败",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onWait() {
            super.onWait();
        }
    };

    /**
     * 微信支付回调
     */
    public void onEventMainThread(Event.WXPayResultEvent event) {
        int code = event.code;
        switch (code) {
            case Event.WXPayResultEvent.CODE_SUCCESS://支付成功
                Toast.makeText(AliWechatPayActivity.this,"支付成功",Toast.LENGTH_SHORT).show();
                break;
            case Event.WXPayResultEvent.CODE_CANCEL://用户取消
                Toast.makeText(AliWechatPayActivity.this,"支付取消",Toast.LENGTH_SHORT).show();
                break;
            case Event.WXPayResultEvent.CODE_FAILED:
                Toast.makeText(AliWechatPayActivity.this,"支付失败",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}

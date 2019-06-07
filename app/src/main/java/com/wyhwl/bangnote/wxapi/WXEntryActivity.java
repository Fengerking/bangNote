package com.wyhwl.bangnote.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import com.wyhwl.bangnote.base.*;
import com.wyhwl.bangnote.wxlogin.*;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI          m_wxAPI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //通过WXAPIFactory工厂获取IWXApI的示例
        m_wxAPI = WXAPIFactory.createWXAPI(this, noteConfig.APP_ID_WX,true);
        //将应用的appid注册到微信
        m_wxAPI.registerApp(noteConfig.APP_ID_WX);

        // 注意：
        // 第三方开发者如果使用透明界面来实现WXEntryActivity，需要判断handleIntent的返回值，
        // 如果返回值为false，则说明入参不合法未被SDK处理，应finish当前透明界面，
        // 避免外部通过传递非法参数的Intent导致停留在透明界面，引起用户的疑惑
        try {
            boolean result =  m_wxAPI.handleIntent(getIntent(), this);
            if(!result){
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        m_wxAPI.handleIntent(data,this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        m_wxAPI.handleIntent(intent, this);
        finish();
    }

    @Override
    public void onReq(BaseReq baseReq) {
     //   ViseLog.d("baseReq:"+ JSON.toJSONString(baseReq));
    }

    @Override
    public void onResp(BaseResp baseResp) {
        WXBaseRespEntity entity = JSON.parseObject(JSON.toJSONString(baseResp),WXBaseRespEntity.class);
        String result = "";
        switch(baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result ="发送成功";
                OkHttpUtils.get().url("https://api.weixin.qq.com/sns/oauth2/access_token")
                        .addParams("appid", noteConfig.APP_ID_WX)
                        .addParams("secret", noteConfig.APP_SECRET_WX)
                        .addParams("code",entity.getCode())
                        .addParams("grant_type","authorization_code")
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(okhttp3.Call call, Exception e, int id) {
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                WXAccessTokenEntity accessTokenEntity = JSON.parseObject(response,WXAccessTokenEntity.class);
                                if(accessTokenEntity!=null){
                                    getUserInfo(accessTokenEntity);
                                }else {

                                }
                            }
                        });
                break;

            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = "发送取消";
                finish();
                break;

            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = "发送被拒绝";
                finish();
                break;

            case BaseResp.ErrCode.ERR_BAN:
                result = "签名错误";
                break;

            default:
                result = "发送返回";
//                showMsg(0,result);
                finish();
                break;
        }

        if (noteConfig.g_nWXLoginResult == 0)
            noteConfig.g_nWXLoginResult = -1;

        //Toast.makeText(WXEntryActivity.this,result, Toast.LENGTH_LONG).show();
    }

    private void getUserInfo(WXAccessTokenEntity accessTokenEntity) {
        //https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
        OkHttpUtils.get()
                .url("https://api.weixin.qq.com/sns/userinfo")
                .addParams("access_token",accessTokenEntity.getAccess_token())
                .addParams("openid",accessTokenEntity.getOpenid())//openid:授权用户唯一标识
                .build()
                .execute(new StringCallback() {
                             @Override
                             public void onError(okhttp3.Call call, Exception e, int id) {
                             }

                             @Override
                             public void onResponse(String response, int id) {
                                 noteConfig.g_nWXLoginResult = 1;
                                 //BaseConfig.g_jsnWXLoginUser = JSON.parseObject(response);

                                 WXUserInfo wxResponse = JSON.parseObject(response,WXUserInfo.class);
                                 String headUrl = wxResponse.getHeadimgurl();
                                 Intent intent = getIntent();
                                 intent.putExtra("headUrl",headUrl);
                                 WXEntryActivity.this.setResult(0,intent);
                                 finish();
                             }
                         }
                );
    }

}

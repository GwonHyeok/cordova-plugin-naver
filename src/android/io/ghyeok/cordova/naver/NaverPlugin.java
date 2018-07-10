package io.ghyeok.cordova.naver;

import android.content.Context;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.data.OAuthLoginState;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class echoes a string called from JavaScript.
 */
public class NaverPlugin extends CordovaPlugin {

    private OAuthLogin mOAuthLoginModule;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();

        // Initialize Naver OAuth Module
        this.mOAuthLoginModule = OAuthLogin.getInstance();
        this.mOAuthLoginModule.init(
                this.cordova.getActivity(),
                this.getString("naver_client_id"),
                this.getString("naver_client_secret"),
                this.getString("naver_client_name")
        );

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("login")) {
            this.executeLogin(callbackContext);
            return true;
        } else if (action.equals("logout")) {
            this.executeLogout(callbackContext);
            return true;
        } else if (action.equals("logoutAndDeleteToken")) {
            this.executeLogoutAndDeleteToken(callbackContext);
            return true;
        } else if (action.equals("refreshAccessToken")) {
            this.executeRefreshAccessToken(callbackContext);
            return true;
        } else if (action.equals("getState")) {
            this.executeGetState(callbackContext);
            return true;
        } else if (action.equals("requestApi")) {
            this.executeRequestApi(args.getString(0), callbackContext);
            return true;
        } else if (action.equals("requestMe")) {
            this.executeRequestMe(callbackContext);
            return true;
        }

        return false;
    }

    /**
     * CallbackContext 와 함께 네이버 로그인을 요청합니다.
     *
     * @param callbackContext callback
     */
    private void executeLogin(final CallbackContext callbackContext) {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NaverPlugin.this.mOAuthLoginModule.startOauthLoginActivity(
                        NaverPlugin.this.cordova.getActivity(),
                        new LoginHandler(NaverPlugin.this.cordova.getActivity(), callbackContext)
                );
            }
        });

    }

    /**
     * 토큰을 지워 로그아웃 처리 합니다.
     *
     * @param callbackContext 콜백
     */
    private void executeLogout(CallbackContext callbackContext) {
        this.mOAuthLoginModule.logout(this.cordova.getActivity());
        callbackContext.success();
    }

    /**
     * 토큰을 지우고, 계정 연동을 해제합니다.
     *
     * @param callbackContext 콜백
     */
    private void executeLogoutAndDeleteToken(CallbackContext callbackContext) {
        Context context = this.cordova.getActivity();
        boolean isSuccessDeleteToken = this.mOAuthLoginModule.logoutAndDeleteToken(this.cordova.getActivity());
        try {
            if (isSuccessDeleteToken) {
                callbackContext.success();
            } else {
                callbackContext.error(buildErrorJsonObject(
                        this.mOAuthLoginModule.getLastErrorCode(context).getCode(),
                        this.mOAuthLoginModule.getLastErrorDesc(context)
                ));
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    /**
     * 클라이언트에 저장된 갱신 토큰(refresh token)을 이용해 접근 토큰(access token)을 갱신하고 갱신된 접근 토큰을 반환합니다.
     *
     * @param callbackContext 콜백
     */
    private void executeRefreshAccessToken(CallbackContext callbackContext) {
        String accessToken = this.mOAuthLoginModule.refreshAccessToken(this.cordova.getActivity());
        callbackContext.success(accessToken);
    }

    /**
     * 네이버 아이디로 로그인 인스턴스의 현재 상태를 반환합니다.
     *
     * @param callbackContext 콜백
     */
    private void executeGetState(CallbackContext callbackContext) {
        OAuthLoginState state = this.mOAuthLoginModule.getState(this.cordova.getActivity());
        callbackContext.success(state.name());
    }

    /**
     * GET 메서드로 API를 호출합니다. 성공하면 결과(content body)를 반환합니다.
     *
     * @param url             API 주소
     * @param callbackContext 콜백
     */
    private void executeRequestApi(String url, CallbackContext callbackContext) {
        Context context = this.cordova.getActivity();
        String accessToken = this.mOAuthLoginModule.getAccessToken(context);
        String response = this.mOAuthLoginModule.requestApi(context, accessToken, url);
        callbackContext.success(response);
    }

    /**
     * 네이버 로그인을 통해 인증받은 받고 정보 제공에 동의한 회원에 대해 회원 메일 주소, 별명, 프로필 사진, 생일, 연령대 값을 조회할 수 있는 로그인 오픈 API입니다.
     * API 호출 결과로 네이버 아이디값은 제공하지 않으며, 대신 'id'라는 애플리케이션당 유니크한 일련번호값을 이용해서 자체적으로 회원정보를 구성하셔야 합니다.
     * 기존 REST API처럼 요청 URL과 요청 변수로 호출하는 방법은 동일하나, OAuth 2.0 인증 기반이므로 추가적으로 네이버 로그인 API를 통해 접근 토큰(access token)을 발급받아,
     * HTTP로 호출할 때 Header에 접근 토큰 값을 전송해 주시면 활용 가능합니다.
     *
     * @param callbackContext 콜백
     */
    private void executeRequestMe(CallbackContext callbackContext) {
        String accessToken = this.mOAuthLoginModule.getAccessToken(this.cordova.getActivity());
        String header = "Bearer " + accessToken;
        try {
            String apiURL = "https://openapi.naver.com/v1/nid/me";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", header);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            if (responseCode == 200) {
                callbackContext.success(buildRequestMeJsonObject(response.toString()));
            } else {
                JSONObject object = new JSONObject(response.toString());
                callbackContext.error(object);
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    /**
     * 리소스 아이디를 문자열로 넘겨 받아 아이디에 해당하는 리소스의 값을 반환합니다.
     *
     * @param id 리소스 아이디
     * @return 리소스에 해당하는 문자열 값
     */
    private String getString(String id) {
        Context context = this.cordova.getActivity();
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(id, "string", packageName);
        return context.getString(resId);
    }

    /**
     * 에러 JSONObject 를 생성해서 반환합니다
     *
     * @param code        에러 코드
     * @param description 에러 설명
     * @return 에러 JsonObject
     * @throws JSONException 에러
     */
    private JSONObject buildErrorJsonObject(String code, String description) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("description", description);
        return jsonObject;
    }

    /**
     * 내 정보 가져오기 API 호출 후 값을 camelCase 로 변경
     *
     * @param response 내 정보
     * @return camelCase 로 변환한 Json 데이터
     * @throws JSONException 에러
     */
    private JSONObject buildRequestMeJsonObject(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        JSONObject responseJsonObject = jsonObject.getJSONObject("response");

        JSONObject serializedObject = new JSONObject();
        JSONObject serializedResponseObject = new JSONObject();

        // 결과 파싱후 camel case 로 수정
        serializedResponseObject.put("email", responseJsonObject.opt("email"));
        serializedResponseObject.put("nickname", responseJsonObject.opt("nickname"));
        serializedResponseObject.put("profileImage", responseJsonObject.opt("profile_image"));
        serializedResponseObject.put("age", responseJsonObject.opt("age"));
        serializedResponseObject.put("gender", responseJsonObject.opt("gender"));
        serializedResponseObject.put("id", responseJsonObject.opt("id"));
        serializedResponseObject.put("name", responseJsonObject.opt("name"));
        serializedResponseObject.put("birthday", responseJsonObject.opt("birthday"));

        // 결과 코드, 메세지
        serializedObject.put("resultCode", jsonObject.get("resultcode"));
        serializedObject.put("message", jsonObject.get("message"));
        serializedObject.put("response", serializedResponseObject);

        return serializedObject;
    }

    /**
     * OAuth Login 실패 성공에 따른 행동을 핸들링하는 클래스
     */
    private class LoginHandler extends OAuthLoginHandler {

        private CallbackContext mCallbackContext;
        private Context mContext;

        LoginHandler(Context context, CallbackContext callbackContext) {
            this.mContext = context;
            this.mCallbackContext = callbackContext;
        }

        @Override
        public void run(boolean isSuccess) {
            JSONObject resultObject = new JSONObject();
            try {
                if (isSuccess) {
                    String accessToken = mOAuthLoginModule.getAccessToken(this.mContext);
                    String refreshToken = mOAuthLoginModule.getRefreshToken(this.mContext);
                    long expiresAt = mOAuthLoginModule.getExpiresAt(this.mContext);
                    String tokenType = mOAuthLoginModule.getTokenType(mContext);

                    // Result JSON 생성
                    resultObject.put("accessToken", accessToken);
                    resultObject.put("refreshToken", refreshToken);
                    resultObject.put("expiresAt", expiresAt);
                    resultObject.put("tokenType", tokenType);

                    // Result Callback
                    this.mCallbackContext.success(resultObject);
                } else {
                    String errorCode = mOAuthLoginModule.getLastErrorCode(this.mContext).getCode();
                    String errorDescription = mOAuthLoginModule.getLastErrorDesc(this.mContext);

                    // Result JSON 생성
                    resultObject.put("code", errorCode);
                    resultObject.put("description", errorDescription);

                    // Result Callback
                    this.mCallbackContext.error(resultObject);
                }
            } catch (Exception e) {
                this.mCallbackContext.error(e.getMessage());
            }
        }
    }

}

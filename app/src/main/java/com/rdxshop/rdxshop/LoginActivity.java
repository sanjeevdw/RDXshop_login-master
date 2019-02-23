package com.rdxshop.rdxshop;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity{
    private DrawerLayout mDrawerLayout;
    private EditText userEmail;
    private EditText userPassword;
    private String userToken;
    private Session session;
    private static final int RC_SIGN_IN = 6;
    private static final String ANONYMOUS = "anonymous";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String TAG = "tag";
    private GoogleSignInClient mGoogleSignInClient;
    boolean doubleBackToExitPressedOnce = false;
    private CallbackManager mCallbackManager;
    private Button loginEmailButton;
    private String username;
    private String tokenFacebook;
    private String emailFacebook;
    private String commonToken;
    private String mUsername;
    private String email;
    private String uidFirebase;
    private String firbaseUsername;
    private static final String EMAIL = "email";
    private String sessionToken;
    private String usernameGoogle;
    private String sessionGoogleEmail;
    private String android_id;
    private WebView myWebView;
    private WebView mWebviewPop;
    private FrameLayout mContainer;
    private Context mContext;
    private String authRedirectURL;
    private ProgressDialog loading = null;
    private String sessionGuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        session = new Session(this);
        sessionToken = session.getusertoken();
        sessionGuest = session.getuserGuest(); loading = new ProgressDialog(this);
       // loading.setMessage("Please wait while loading");

        if (!sessionGuest.isEmpty()) {
            Intent intentMainActivity = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intentMainActivity);
        }
        else if (!sessionToken.isEmpty()) {
            Intent intentMainActivity = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intentMainActivity);
        }

        setContentView(R.layout.activity_login_new);

        userEmail = (EditText) findViewById(R.id.et_enter_email);
        userPassword = (EditText) findViewById(R.id.et_enter_password);

        TextView guestTextView = (TextView) findViewById(R.id.textView_guest_continue);
        guestTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.setuserGuest("");
                session.setuserGuest("1");
                loading = new ProgressDialog(LoginActivity.this);
                loading.setMessage("Please wait while loading");
                setContentView(R.layout.activity_main);
                myWebView = (WebView) findViewById(R.id.webview);
                myWebView.loadUrl("https://www.rdxshop.com/");
                WebSettings webSettings = myWebView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                myWebView.setWebViewClient(new MyWebViewClient());
                myWebView.setWebChromeClient(new MyWebChromeClient());
                myWebView.addJavascriptInterface(new WebAppInterface(LoginActivity.this), "Android");
                myWebView.getSettings().setDomStorageEnabled(true);
                //Cookie manager for the webview
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);

                //Get outer container
                mContainer = (FrameLayout) findViewById(R.id.webview_frame);
                webSettings.setAppCacheEnabled(true);
                webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                webSettings.setSupportMultipleWindows(true);

                myWebView.setWebViewClient(new MyCustomWebViewClient());
                myWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

                myWebView.setWebChromeClient(new MyCustomChromeClient());

            }
        });

        Button signinButton = (Button) findViewById(R.id.button_sign_in);
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginNetworkRequest();
            }
        });

        session = new Session(this);
        sessionToken = session.getusertoken();
        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        mContext=this.getApplicationContext();

        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess" + loginResult);
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.d("LoginActivity", response.toString());
                                String email = object.optString("email");
                                String name = object.optString("name");
                                Log.d("LoginActivity", email);
                                String id = object.optString("id");
                              //  registerFacebookNetworkRequest(name, email);
                                sessionGoogleEmail = "";
                                sessionGoogleEmail = email;
                                gmailLoginNetworkRequest();
                                Log.d("LoginActivity", name);
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, email");
                request.setParameters(parameters);
                request.executeAsync();
               handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch(v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                }
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
          //  ramdomId = toast;
          //  ButtonNetworkRequest(ramdomId);
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void showGoogleLogin(String toast) {
            authRedirectURL = toast;
            signIn();
        }

        @JavascriptInterface
        public void showLogout() {
            logoutNetworkRequest();
        }

        @JavascriptInterface
        public String getReturn() {
            if (!sessionToken.isEmpty()) {
                sessionToken = session.getusertoken();
            }
            JSONObject sessionHandle = new JSONObject();
            try {
                sessionHandle.put("android_id", android_id);
                sessionHandle.put("session_token", sessionToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String returnSession = sessionHandle.toString();
            Log.d(TAG, returnSession);
            return returnSession;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void returnToWebViewFromChrome(String auth) {
            Toast.makeText(mContext, auth, Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_main);
            myWebView = (WebView) findViewById(R.id.webview);
            myWebView.loadUrl("https://www.rdxshop.com/");
            WebSettings webSettings = myWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            myWebView.setWebViewClient(new MyWebViewClient());
            myWebView.addJavascriptInterface(new WebAppInterface(LoginActivity.this), "Android");
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            Toast.makeText(this, "Signed In.", Toast.LENGTH_SHORT).show();
        } else if (requestCode == RESULT_CANCELED) {
            Toast.makeText(this, "Sign in cancelled.", Toast.LENGTH_SHORT).show();
          //  finish();
            setContentView(R.layout.activity_main);
            myWebView = (WebView) findViewById(R.id.webview);
            loading = new ProgressDialog(LoginActivity.this);
            loading.setMessage("Please wait loading...");
            loading.show();
            myWebView.loadUrl("https://www.rdxshop.com/login.php");
            WebSettings webSettings = myWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            myWebView.setWebViewClient(new MyWebViewClient() );
            myWebView.setWebChromeClient(new MyWebChromeClient());
            myWebView.addJavascriptInterface(new WebAppInterface(LoginActivity.this), "Android");
            myWebView.getSettings().setDomStorageEnabled(true);
            //Cookie manager for the webview
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);

            //Get outer container
            mContainer = (FrameLayout) findViewById(R.id.webview_frame);
            webSettings.setAppCacheEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setSupportMultipleWindows(true);

            myWebView.setWebViewClient(new MyCustomWebViewClient());
            myWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

            myWebView.setWebChromeClient(new MyCustomChromeClient());
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                usernameGoogle = account.getDisplayName();
                sessionGoogleEmail = account.getEmail();
                gmailLoginNetworkRequest();
                //     registerNetworkRequest(usernameGoogle, sessionGoogleEmail);
            //    updateUI(account);
            }
            else {

            }
        }catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
           // updateUI(null);
        }
    }

    public class MyWebChromeClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
            return super.onJsAlert(view, url, message, result);
        };
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce= false;
            }
        }, 2000);
    }

    private void gmailLoginNetworkRequest() {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.rdxshop.com/api/loginwithgoogleplus.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            String jsonResponse = response.toString().trim();
                            //   jsonResponse = jsonResponse.substring(3);
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            String status = jsonObject.getString("status");
                            String tokenApi = jsonObject.getString("tokenApi");
                            String authToken = jsonObject.getString("auth_token");
                            session.setusertoken("");
                            session.setusertoken(tokenApi);
                            sessionToken = session.getusertoken();
                            setContentView(R.layout.activity_main);
                            myWebView = (WebView) findViewById(R.id.webview);
                            myWebView.loadUrl("https://www.rdxshop.com/?"+"&auth_token="+authToken+"&tokenApi="+tokenApi);
                            WebSettings webSettings = myWebView.getSettings();
                            webSettings.setJavaScriptEnabled(true);
                            myWebView.setWebViewClient(new MyWebViewClient());
                            myWebView.setWebChromeClient(new MyWebChromeClient());
                            myWebView.addJavascriptInterface(new WebAppInterface(LoginActivity.this), "Android");
                            myWebView.getSettings().setDomStorageEnabled(true);
                            //Cookie manager for the webview
                            CookieManager cookieManager = CookieManager.getInstance();
                            cookieManager.setAcceptCookie(true);

                            //Get outer container
                            mContainer = (FrameLayout) findViewById(R.id.webview_frame);
                            webSettings.setAppCacheEnabled(true);
                            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                            webSettings.setSupportMultipleWindows(true);

                            myWebView.setWebViewClient(new MyCustomWebViewClient());
                            myWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

                            myWebView.setWebChromeClient(new MyCustomChromeClient());
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
            }

        }) { @Override
        protected Map<String, String> getParams() {
            Map<String, String> params = new HashMap<String, String>();
            params.put("deviceId", android_id);
            params.put("email", sessionGoogleEmail);
            return params;
        }
        };
        queue.add(stringRequest);
    }


    private void loginNetworkRequest() {

        final String userEnteredEmail = userEmail.getText().toString().trim();
        final String userEnteredPassword = userPassword.getText().toString().trim();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.rdxshop.com/api/makeitlogin.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            String jsonResponse = response.toString().trim();
                            //   jsonResponse = jsonResponse.substring(3);
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            String status = jsonObject.getString("status");

                            int statusInt = Integer.parseInt(status);
                            if (statusInt == 200) {
                                String tokenApi = jsonObject.getString("tokenApi");
                                String authToken = jsonObject.getString("auth_token");
                                session.setusertoken("");
                                session.setusertoken(tokenApi);
                                sessionToken = session.getusertoken();
                                setContentView(R.layout.activity_main);
                                myWebView = (WebView) findViewById(R.id.webview);
                                myWebView.loadUrl("https://www.rdxshop.com/?"+"&auth_token="+authToken+"&tokenApi="+tokenApi);
                                WebSettings webSettings = myWebView.getSettings();
                                webSettings.setJavaScriptEnabled(true);
                                myWebView.setWebViewClient(new MyWebViewClient());
                                myWebView.setWebChromeClient(new MyWebChromeClient());
                                myWebView.addJavascriptInterface(new WebAppInterface(LoginActivity.this), "Android");
                                myWebView.getSettings().setDomStorageEnabled(true);
                                //Cookie manager for the webview
                                CookieManager cookieManager = CookieManager.getInstance();
                                cookieManager.setAcceptCookie(true);

                                //Get outer container
                                mContainer = (FrameLayout) findViewById(R.id.webview_frame);
                                webSettings.setAppCacheEnabled(true);
                                webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                                webSettings.setSupportMultipleWindows(true);

                                myWebView.setWebViewClient(new MyCustomWebViewClient());
                                myWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

                                myWebView.setWebChromeClient(new MyCustomChromeClient());

                            } else if (statusInt == 201) {
                                //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                String message = jsonObject.getString("message");
                                LinearLayout linearLayoutError = (LinearLayout) findViewById(R.id.response_message_linear);
                                linearLayoutError.setVisibility(View.VISIBLE);
                                TextView responseTextViewTwo = (TextView) findViewById(R.id.response_message_two);
                                responseTextViewTwo.setText(message);
                            }


                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
            }

        }) { @Override
        protected Map<String, String> getParams() {
            Map<String, String> params = new HashMap<String, String>();
            params.put("deviceId", android_id);
            params.put("email", userEnteredEmail);
            params.put("password", userEnteredPassword);
            return params;
        }
        };
        queue.add(stringRequest);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                // connected to the internet
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    if (url.startsWith("https://www.rdxshop.com/payment.php")) {
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (Uri.parse(url).getHost().equals("www.rdxshop.com")) {
                        // This is my website, so do not override; let my WebView load the page
                        return false;
                    }

                    else if (url.startsWith("https://www.rdxshop.com/payment-pay.php")) {
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (url.startsWith("https://securegw.paytm.in/theia/processTransaction")) {
                        // This is my website, so do not override; let my WebView load the page
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (url.startsWith("https://www.rdxshop.com/pay/paytm/index.php?tokenid=")) {
                        // This is my website, so do not override; let my WebView load the page
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }


                    else if (url.startsWith("https://www.rdxshop.com/payumoney/")) {
                        // This is my website, so do not override; let my WebView load the page
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (url.startsWith("https://www.rdxshop.com/facebook/")) {
                        // This is my website, so do not override; let my WebView load the page
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (url.startsWith("https://www.rdxshop.com/google/")) {
                        // This is my website, so do not override; let my WebView load the page
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (Uri.parse(url).getHost().equals("paytm.in")) {
                        // This is my website, so do not override; let my WebView load the page
                        return false;
                    }

                    else if (Uri.parse(url).getHost().equals("accounts.paytm.in")) {
                        // This is my website, so do not override; let my WebView load the page
                        return false;
                    }

                    else if (Uri.parse(url).getHost().equals("accounts.paytm.com")) {
                        // This is my website, so do not override; let my WebView load the page
                        return false;
                    }

                    else if (Uri.parse(url).getHost().equals("paytm.com")) {
                        // This is my website, so do not override; let my WebView load the page
                        return false;
                    }

                    else if (Uri.parse(url).getHost().equals("sandboxsecure.payu.in")) {
                        // This is my website, so do not override; let my WebView load the page
                        return false;
                    }

                }

                else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    // connected to mobile data

                    if (url.startsWith("https://www.rdxshop.com/payment.php")) {
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (Uri.parse(url).getHost().equals("www.rdxshop.com")) {
                        // This is my website, so do not override; let my WebView load the page
                        return false;
                    }

                    else if (url.startsWith("https://www.rdxshop.com/payment-pay.php")) {
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (url.startsWith("https://securegw.paytm.in/theia/processTransaction")) {
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (url.startsWith("https://www.rdxshop.com/pay/paytm/index.php?tokenid=")) {
                        // This is my website, so do not override; let my WebView load the page
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (url.startsWith("https://www.rdxshop.com/facebook/")) {
                        // This is my website, so do not override; let my WebView load the page
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (url.startsWith("https://www.rdxshop.com/google/")) {
                        // This is my website, so do not override; let my WebView load the page
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (url.startsWith("https://www.rdxshop.com/payumoney/")) {
                        // This is my website, so do not override; let my WebView load the page
                        Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(cartIntent);
                        return true;
                    }

                    else if (Uri.parse(url).getHost().equals("paytm.in")) {
                        // This is my website, so do not override; let my WebView load the page
                        return false;
                    }


                    else if (Uri.parse(url).getHost().equals("accounts.paytm.in")) {
                        // This is my website, so do not override; let my WebView load the page
                        return false;
                    }

                    else if (Uri.parse(url).getHost().equals("accounts.paytm.com")) {
                        // This is my website, so do not override; let my WebView load the page
                        return false;
                    }

                    else if (Uri.parse(url).getHost().equals("paytm.com")) {
                        // This is my website, so do not override; let my WebView load the page
                        return false;
                    }

                }
            } else {
                // not connected to the internet
                setContentView(R.layout.activity_empty);
                TextView emptyTextView = (TextView) findViewById(R.id.empty_view);
                emptyTextView.setText(R.string.no_internet_connection);
                Button reloadButtonNetConnected = (Button) findViewById(R.id.re_load_button);
                reloadButtonNetConnected.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                        if (activeNetwork != null) {
                            // connected to the internet
                            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                                // connected to wifi
                                setContentView(R.layout.activity_main);
                                myWebView = (WebView) findViewById(R.id.webview);
                                myWebView.loadUrl("https://www.rdxshop.com/");
                                WebSettings webSettings = myWebView.getSettings();
                                webSettings.setJavaScriptEnabled(true);
                                myWebView.setWebViewClient(new MyWebViewClient());
                                myWebView.setWebChromeClient(new MyWebChromeClient());
                                myWebView.addJavascriptInterface(new WebAppInterface(LoginActivity.this), "Android");
                                myWebView.getSettings().setDomStorageEnabled(true);
                                //Cookie manager for the webview
                                CookieManager cookieManager = CookieManager.getInstance();
                                cookieManager.setAcceptCookie(true);

                                //Get outer container
                                mContainer = (FrameLayout) findViewById(R.id.webview_frame);
                                webSettings.setAppCacheEnabled(true);
                                webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                                webSettings.setSupportMultipleWindows(true);

                                myWebView.setWebViewClient(new MyCustomWebViewClient());
                                myWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

                                myWebView.setWebChromeClient(new MyCustomChromeClient());

                            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                                // connected to mobile data
                                setContentView(R.layout.activity_main);
                                myWebView = (WebView) findViewById(R.id.webview);
                                myWebView.loadUrl("https://www.rdxshop.com/");
                                WebSettings webSettings = myWebView.getSettings();
                                webSettings.setJavaScriptEnabled(true);
                                myWebView.setWebViewClient(new MyWebViewClient());
                                myWebView.setWebChromeClient(new MyWebChromeClient());
                                myWebView.addJavascriptInterface(new WebAppInterface(LoginActivity.this), "Android");
                                myWebView.getSettings().setDomStorageEnabled(true);
                                //Cookie manager for the webview
                                CookieManager cookieManager = CookieManager.getInstance();
                                cookieManager.setAcceptCookie(true);

                                //Get outer container
                                mContainer = (FrameLayout) findViewById(R.id.webview_frame);
                                webSettings.setAppCacheEnabled(true);
                                webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                                webSettings.setSupportMultipleWindows(true);

                                myWebView.setWebViewClient(new MyCustomWebViewClient());
                                myWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

                                myWebView.setWebChromeClient(new MyCustomChromeClient());

                            }
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Please connect to the Internet", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }

              /*  @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    if (!loading.isShowing()) {
                        loading.show();
                    }
                    } */

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (loading.isShowing()) {
                loading.dismiss();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        else if ((keyCode == KeyEvent.KEYCODE_FORWARD) && myWebView.canGoForward()) {
            myWebView.goForward();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    private class MyCustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String host = Uri.parse(url).getHost();
            //Log.d("shouldOverrideUrlLoading", url);

            if (url.startsWith("https://www.rdxshop.com/payment.php")) {
                Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(cartIntent);
                return true;
            }

            else if (host.equals("www.rdxshop.com"))
            {
                // This is my web site, so do not override; let my WebView load
                // the page
                if(mWebviewPop!=null)
                {
                    mWebviewPop.setVisibility(View.GONE);
                    mContainer.removeView(mWebviewPop);
                    mWebviewPop=null;
                }
                return false;
            }

            if(host.equals("m.facebook.com") || host.equals("www.facebook.com"))
            {
                return false;
            }

            if(host.equals("accounts.google.com") || host.equals("google.com"))
            {
                return true;
            }

            else if (url.startsWith("https://www.rdxshop.com/payment-pay.php")) {
                Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(cartIntent);
                return true;
            }

            else if (url.startsWith("https://securegw.paytm.in/theia/processTransaction")) {
                // This is my website, so do not override; let my WebView load the page
                Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(cartIntent);
                return true;
            }

            else if (url.startsWith("https://www.rdxshop.com/pay/paytm/index.php?tokenid=")) {
                // This is my website, so do not override; let my WebView load the page
                Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(cartIntent);
                return true;
            }


            else if (url.startsWith("https://www.rdxshop.com/payumoney/")) {
                // This is my website, so do not override; let my WebView load the page
                Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(cartIntent);
                return true;
            }

            else if (url.startsWith("https://www.rdxshop.com/facebook/")) {
                // This is my website, so do not override; let my WebView load the page
                Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(cartIntent);
                return true;
            }

            else if (url.startsWith("https://www.rdxshop.com/google/")) {
                // This is my website, so do not override; let my WebView load the page
                Intent cartIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(cartIntent);
                return true;
            }

            else if (Uri.parse(url).getHost().equals("paytm.in")) {
                // This is my website, so do not override; let my WebView load the page
                return false;
            }

            else if (Uri.parse(url).getHost().equals("accounts.paytm.in")) {
                // This is my website, so do not override; let my WebView load the page
                return false;
            }

            else if (Uri.parse(url).getHost().equals("accounts.paytm.com")) {
                // This is my website, so do not override; let my WebView load the page
                return false;
            }

            else if (Uri.parse(url).getHost().equals("paytm.com")) {
                // This is my website, so do not override; let my WebView load the page
                return false;
            }

            else if (Uri.parse(url).getHost().equals("sandboxsecure.payu.in")) {
                // This is my website, so do not override; let my WebView load the page
                return false;
            }

            // Otherwise, the link is not for a page on my site, so launch
            // another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            Log.d("onReceivedSslError", "onReceivedSslError");
            //super.onReceivedSslError(view, handler, error);
        }

        //   @Override
        //   public void onPageStarted(WebView view, String url, Bitmap favicon) {
        //       super.onPageStarted(view, url, favicon);
        //       }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (loading.isShowing()) {
                loading.dismiss();
            }
        }

    }

    private class MyCustomChromeClient extends WebChromeClient
    {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            mWebviewPop = new WebView(mContext);
            mWebviewPop.setVerticalScrollBarEnabled(false);
            mWebviewPop.setHorizontalScrollBarEnabled(false);
            mWebviewPop.setWebViewClient(new MyCustomWebViewClient());
            mWebviewPop.getSettings().setJavaScriptEnabled(true);
            mWebviewPop.getSettings().setSavePassword(false);
            mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mWebviewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebviewPop);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            Log.d("onCloseWindow", "called");
        }
    }

    private void logoutNetworkRequest() {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.rdxshop.com/api/makeitlogout.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            String jsonResponse = response.toString().trim();
                            //   jsonResponse = jsonResponse.substring(3);
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
            }

        }) { @Override
        protected Map<String, String> getParams() {
            Map<String, String> params = new HashMap<String, String>();
            params.put("deviceId", android_id);
            params.put("tokenApi", sessionToken);
            return params;
        }
        };
        queue.add(stringRequest);
    }

}

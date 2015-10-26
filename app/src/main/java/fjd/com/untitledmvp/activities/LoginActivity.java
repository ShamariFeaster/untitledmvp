package fjd.com.untitledmvp.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.facebook.FacebookSdk;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.ValueEventListener;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import fjd.com.untitledmvp.R;
import fjd.com.untitledmvp.models.User;
import fjd.com.untitledmvp.state.GlobalState;
import fjd.com.untitledmvp.util.Constants;

/**
 * A login screen that offers login via email/password.
 * https://nitework.firebaseio.com/
 */
public class LoginActivity extends Activity  {


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AsyncTask<Void, Void, Boolean> mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private CallbackManager callbackManager;
    private Firebase mFBRef;
    private AccessToken mFBKToken = null;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mFBRef = new Firebase(Constants.FBURL);
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);

        authWithFBToken(AccessToken.getCurrentAccessToken());

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        Button mEmailSignUpButton = (Button) findViewById(R.id.email_sign_up_btn);

        mEmailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthData auth = mFBRef.getAuth();
                if(auth == null){
                    showProgress(true);
                    signUpWithEmail();
                }else{
                    String provider = (String) auth.getAuth().get("provider");
                    Log.d(TAG, "Already logged in with " + provider);
                }

            }
        });

        Button EmailSignInButton = (Button) findViewById(R.id.email_sign_in_btn);
        EmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthData auth = mFBRef.getAuth();
                if (auth == null) {
                    showProgress(true);
                    signInWithEmail();
                } else {
                    String provider = (String) auth.getAuth().get("provider");
                    Log.d(TAG, "Already logged in with " + provider);
                }
            }
        });

        Button LogoutBtn = (Button) findViewById(R.id.logout);
        LogoutBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                mFBRef.unauth();
                AccessToken token = AccessToken.getCurrentAccessToken();
                /* make the API call */
                new GraphRequest(
                        token,
                        "/" + token.getUserId() + "/permissions",
                        null,
                        HttpMethod.DELETE,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                showProgress(false);
                            }
                        }
                ).executeAsync();
            }
        });

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
            }
        });
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                showProgress(false);
                AuthData auth = mFBRef.getAuth();
                if(auth == null){
                    Log.d(TAG, "Authenticating with Facebook");
                    authWithFBToken(loginResult.getAccessToken());
                }else{
                    Log.d(TAG, "User already authenticated by other means");
                }

            }

            @Override
            public void onCancel() {
                showProgress(false);
                Log.d(TAG, "Facebook Login Canceled");
            }

            @Override
            public void onError(FacebookException exception) {
                showProgress(false);
                Log.d(TAG, "Facebook Login failed: " + exception.getMessage());
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void authWithFBToken(AccessToken token){
        final AccessToken _token = token;

        if (_token != null) {
            mFBRef.authWithOAuthToken("facebook", token.getToken(), new Firebase.AuthResultHandler() {

                @Override
                public void onAuthenticated(AuthData authData) {
                    Log.d(TAG, "Firebase Authenticated with Facebook");

                    final String _FIBuid = authData.getUid();

                    final Bundle params = new Bundle();
                    params.putString("fields","email,first_name,last_name");

                    new GraphRequest(
                            _token,
                            "/"+ _token.getUserId(),
                            params,
                            HttpMethod.GET,
                            new GraphRequest.Callback() {
                                public void onCompleted(GraphResponse response) {
                                    JSONObject graphResponse = response.getJSONObject();
                                    String fn = "";
                                    String ln = "";
                                    String email = "";
                                    try {
                                        fn = graphResponse.getString("first_name");
                                        ln = graphResponse.getString("last_name");
                                        email = graphResponse.getString("email");
                                    }catch(JSONException ex){
                                        Log.v(TAG, "GraphResponse failure: " + ex.getMessage());
                                    }
                                    //firebase id is "facebook:<facebook uid>"
                                    initProfileInDb(_FIBuid, email, fn, ln);
                                }
                            }
                    ).executeAsync();


                }
                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    Log.d(TAG, "ERROR: Firebase Not Authenticated with Facebook");
                }
            });
        } else {
        /* Logged out of Facebook so do a logout from the Firebase app */
            mFBRef.unauth();
        }

    }



    public void signInWithEmail(){
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        mAuthTask = new UserSignInTask( mEmailView.getText().toString(), mPasswordView.getText().toString(), mFBRef);
        mAuthTask.execute((Void) null);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void signUpWithEmail() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
            mAuthTask = new UserSignUpTask(email, password, mFBRef);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void initProfileInDb(String uid, String email){
        Firebase ref = new Firebase(Constants.FBURL+"/users/"+ uid);
        //surround with try/catch
        GlobalState state = (GlobalState) getApplicationContext();
        state.CurrUser = new User(email, "","",email);;
        state.CurrUser.uid = uid;
        ref.setValue(state.CurrUser);
    }

    public void initProfileInDb(final String uid, final String email, final String fn, final String ln){
        final Firebase ref = new Firebase(Constants.FBURL+"/users/"+ uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    GlobalState state = (GlobalState) getApplicationContext();
                    state.CurrUser = new User(email, fn, ln, email);
                    state.CurrUser.uid = uid;
                    ref.setValue(state.CurrUser);
                }

                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        //surround with try/catch


    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserSignUpTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private Boolean mCreateSuccess = true;
        private Firebase mFBRef;
        UserSignUpTask(String email, String password, Firebase ref) {
            mEmail = email;
            mPassword = password;
            mFBRef = ref;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                //Firebase ref = new Firebase("https://nitework.firebaseio.com");
                mFBRef.createUser(mEmail, mPassword, new Firebase.ValueResultHandler<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        initProfileInDb((String) result.get("uid"), mEmail);
                        mAuthTask = null;
                        showProgress(false);
                        Log.d(TAG, "_Successfully created user account with uid: " + result.get("uid"));
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        // there was an error
                        switch (firebaseError.getCode()){
                            case FirebaseError.EMAIL_TAKEN:
                                mEmailView.setError(getString(R.string.error_email_taken));
                                mEmailView.requestFocus();
                                mCreateSuccess = false;
                                break;
                            case FirebaseError.INVALID_EMAIL:
                                mEmailView.setError(getString(R.string.error_invalid_email));
                                mEmailView.requestFocus();
                                mCreateSuccess = false;
                                break;
                            case FirebaseError.INVALID_PASSWORD:
                                mPasswordView.setError(getString(R.string.error_pw_wrong));
                                mPasswordView.requestFocus();
                                mCreateSuccess = false;
                                break;
                            case FirebaseError.NETWORK_ERROR:
                                break;
                            case FirebaseError.OPERATION_FAILED:
                                break;
                            case FirebaseError.PREEMPTED:
                                break;
                            case FirebaseError.PROVIDER_ERROR:
                                break;
                            case FirebaseError.UNAVAILABLE:
                                break;
                            case FirebaseError.UNKNOWN_ERROR:
                                break;
                            case FirebaseError.USER_DOES_NOT_EXIST:
                                break;
                        }
                    }
                });
            } catch (Exception e) {
                mCreateSuccess = false;
            }

            return mCreateSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                //finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class UserSignInTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private Boolean mCreateSuccess = true;
        private Firebase mFBRef;
        UserSignInTask(String email, String password, Firebase ref) {
            mEmail = email;
            mPassword = password;
            mFBRef = ref;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                //Firebase ref = new Firebase("https://nitework.firebaseio.com");
                mFBRef.authWithPassword(mEmail, mPassword, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        Log.d(TAG, "_User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                        mAuthTask = null;
                        showProgress(false);
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        // there was an error
                        switch (firebaseError.getCode()) {
                            case FirebaseError.INVALID_EMAIL:
                                mEmailView.setError(getString(R.string.error_invalid_email));
                                mEmailView.requestFocus();
                                mCreateSuccess = false;
                                break;
                            case FirebaseError.INVALID_PASSWORD:
                                mPasswordView.setError(getString(R.string.error_pw_wrong));
                                mPasswordView.requestFocus();
                                mCreateSuccess = false;
                                break;
                            case FirebaseError.NETWORK_ERROR:
                                break;
                            case FirebaseError.OPERATION_FAILED:
                                break;
                            case FirebaseError.PREEMPTED:
                                break;
                            case FirebaseError.PROVIDER_ERROR:
                                break;
                            case FirebaseError.UNAVAILABLE:
                                break;
                            case FirebaseError.UNKNOWN_ERROR:
                                break;
                            case FirebaseError.USER_DOES_NOT_EXIST:
                                mEmailView.setError(getString(R.string.error_user_no_exist));
                                mEmailView.requestFocus();
                                mCreateSuccess = false;
                                break;
                        }
                    }
                });
            } catch (Exception e) {
                mCreateSuccess = false;
            }

            return mCreateSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class UserSignInFacebookTask extends AsyncTask<Void, Void, Boolean> {


        private Boolean mCreateSuccess = true;
        UserSignInFacebookTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                Firebase ref = new Firebase("https://nitework.firebaseio.com");

            } catch (Exception e) {
                mCreateSuccess = false;
            }

            return mCreateSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}


package dk.grixie.oauth2.rest.oauth2;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Validator {
    private static final Logger log = LoggerFactory.getLogger(Validator.class);

    private static Validator instance = null;

    private String id;
    private String password;
    private URL validateEndpoint;

    public static synchronized void initialize(String id, String password, URL validateEndpoint) {
        instance = new Validator(id, password, validateEndpoint);
    }

    public static synchronized Validator getInstance() {
        return instance;
    }

    private Validator(String id, String password, URL validateEndpoint) {
        this.id = id;
        this.password = password;
        this.validateEndpoint = validateEndpoint;
    }

    public Validation validateAccessToken(String accessTokenId) throws URISyntaxException, IOException, JSONException {
        HttpClient client = HttpClientBuilder.create().build();

        HttpPost post = new HttpPost(validateEndpoint.toURI());

        if (password != null) {
            post.addHeader(createBasicAuthorizationHeader(id, password));
        }

        List<NameValuePair> values = new ArrayList<>();
        values.add(new BasicNameValuePair("access_token", accessTokenId));

        post.setEntity(new UrlEncodedFormEntity(values, "UTF-8"));

        HttpResponse reply = client.execute(post);

        StringBuilder result = new StringBuilder();

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(reply.getEntity().getContent()))) {
            String line;

            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        }

        JSONObject response = new JSONObject(result.toString());

        if (response.has("status") && response.getString("status").equalsIgnoreCase("valid_token")) {
            String userId = null;
            long expiresAt = 0;
            Collection<String> scope = new ArrayList<>();

            if (response.has("expires_in")) {
                expiresAt = new Date().getTime() + response.getLong("expires_in");
            }

            if (response.has("userId")) {
                userId = response.getString("userId");
            }

            if (response.has("scopes")) {
                JSONArray array = response.getJSONArray("scopes");

                for (int i = 0; i < array.length(); i++) {
                    if (array.getString(i) != null) {
                        scope.add(array.getString(i));
                    }
                }
            }

            return new Validation(accessTokenId, true, userId, expiresAt, scope);
        } else {
            return new Validation(accessTokenId, false, null, 0, new ArrayList<>());
        }
    }

    private Header createBasicAuthorizationHeader(String clientId, String password)
            throws UnsupportedEncodingException {
        return new BasicHeader("Authorization", "Basic " +
                new String(Base64.encodeBase64((clientId + ":" + password).getBytes("UTF-8")), "UTF-8"));
    }
}

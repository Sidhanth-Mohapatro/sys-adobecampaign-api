package com.central;

import static java.lang.Boolean.TRUE;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class IMSClient {
	public static void main(String[] args)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, URISyntaxException {

/**		
		Properties prop = new Properties();

		InputStream input = IMSClient.class.getClassLoader().getResourceAsStream("jwt-config-dev.properties");
		// load a properties file
		prop.load(input);

		String jwtToken = getJWTToken(prop);
		System.out.println("JWT Token:\n" + jwtToken);

//			if (jwtToken != null && jwtToken != "") {
//				System.out.println("JWT Token: " + jwtToken);
//				String accessToken = getAccessToken(prop, jwtToken);
//				System.out.println("Access Token: " + accessToken);
//			}

*/
		
		String rs256JWT = getJWTToken(
				"CC3A3CEE5CC2D8C10A495EE2@AdobeOrg", // orgId
				"C52C33DB5FFA810A0A495C0F@techacct.adobe.com", // technicalAccountId
				"b8dad71d05354326b639681deaa66a1c", // apiKey
				"/adobe-pkcs8-private.key", // key_path
				"ims-na1.adobelogin.com", //imsHost
				"ent_campaign_sdk"  // metascopesString
		);
		
		System.out.println("rs256JWT:\n" + rs256JWT);
		
	}
	
	
	public static String getJWTToken(String orgId, String technicalAccountId, String apiKey, String keyPath, String imsHost, String metascopesString)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, URISyntaxException {
		// Load relevant properties from prop file
		// Expiration time in seconds
		Long expirationTime = System.currentTimeMillis() / 1000 + 86400L;
		// Metascopes associated to key
		String metascopes[] = metascopesString.split(",");

		// # create the certificate and private key using openssl
		// $ openssl req -nodes -text -x509 -newkey rsa:2048 -keyout secret.pem -out
		// certificate.pem -days 356
		//
		// Upload the certificate.pem in Adobe IO Console-> Your Integration-> Public
		// keys
		//
		// # convert private key to DER format
		// $ openssl pkcs8 -topk8 -inform PEM -outform DER -in secret.pem -nocrypt >
		// secret.key

		// Secret key as byte array. Secret key file should be in DER encoded format.
		// byte[] privateKeyFileContent = Files.readAllBytes(Paths.get(keyPath));
		byte[] privateKeyFileContent = Files.readAllBytes(Paths.get(IMSClient.class.getResource(keyPath).toURI()));

		//

		// Read the private key
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		KeySpec ks = new PKCS8EncodedKeySpec(privateKeyFileContent);
		RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(ks);

		// Create JWT payload
		Map<String, Object> jwtClaims = new HashMap<String, Object>();
		jwtClaims.put("iss", orgId);
		jwtClaims.put("sub", technicalAccountId);
		jwtClaims.put("exp", expirationTime);
		jwtClaims.put("aud", "https://" + imsHost + "/c/" + apiKey);
		for (String metascope : metascopes) {
			jwtClaims.put("https://" + imsHost + "/s/" + metascope, TRUE);
		}

		SignatureAlgorithm sa = SignatureAlgorithm.RS256;
		// Create the final JWT token
		String jwtToken = Jwts.builder().setClaims(jwtClaims).signWith(sa, privateKey).compact();

		return jwtToken;
	}	

	public static String getJWTToken(Properties prop)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, URISyntaxException {
		// Load relevant properties from prop file
		String orgId = prop.getProperty("orgId");
		String technicalAccountId = prop.getProperty("technicalAccountId");
		String apiKey = prop.getProperty("apiKey");
		String keyPath = prop.getProperty("key_path");
		String imsHost = prop.getProperty("imsHost");
		// Expiration time in seconds
		Long expirationTime = System.currentTimeMillis() / 1000 + 86400L;
		// Metascopes associated to key
		String metascopes[] = prop.getProperty("metascopes").split(",");

		// # create the certificate and private key using openssl
		// $ openssl req -nodes -text -x509 -newkey rsa:2048 -keyout secret.pem -out
		// certificate.pem -days 356
		//
		// Upload the certificate.pem in Adobe IO Console-> Your Integration-> Public
		// keys
		//
		// # convert private key to DER format
		// $ openssl pkcs8 -topk8 -inform PEM -outform DER -in secret.pem -nocrypt >
		// secret.key

		// Secret key as byte array. Secret key file should be in DER encoded format.
		// byte[] privateKeyFileContent = Files.readAllBytes(Paths.get(keyPath));
		byte[] privateKeyFileContent = Files.readAllBytes(Paths.get(IMSClient.class.getResource(keyPath).toURI()));

		//

		// Read the private key
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		KeySpec ks = new PKCS8EncodedKeySpec(privateKeyFileContent);
		RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(ks);

		// Create JWT payload
		Map<String, Object> jwtClaims = new HashMap<String, Object>();
		jwtClaims.put("iss", orgId);
		jwtClaims.put("sub", technicalAccountId);
		jwtClaims.put("exp", expirationTime);
		jwtClaims.put("aud", "https://" + imsHost + "/c/" + apiKey);
		for (String metascope : metascopes) {
			jwtClaims.put("https://" + imsHost + "/s/" + metascope, TRUE);
		}

		SignatureAlgorithm sa = SignatureAlgorithm.RS256;
		// Create the final JWT token
		String jwtToken = Jwts.builder().setClaims(jwtClaims).signWith(sa, privateKey).compact();

		return jwtToken;
	}

	public static String getAccessToken(Properties prop, String jwtToken) throws IOException {
		// Load relevant properties from prop file
		String accessToken = "";
		String imsExchange = prop.getProperty("imsExchange");
		String apiKey = prop.getProperty("apiKey");
		String secret = prop.getProperty("secret");

		URL obj = new URL(imsExchange);

		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		// add request header
		con.setRequestMethod("POST");

		// Add parameters to request
		String urlParameters = "client_id=" + apiKey + "&client_secret=" + secret + "&jwt_token=" + jwtToken;

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("Sending 'POST' request to URL: " + imsExchange);
		System.out.println("Post parameters: " + urlParameters);
		System.out.println("Response Code: " + responseCode);

		boolean responseError = false;
		InputStream is;
		if (responseCode < HttpsURLConnection.HTTP_BAD_REQUEST) {
			is = con.getInputStream();
		} else {
			/* error from server */
			is = con.getErrorStream();
			responseError = true;
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		if (responseError) {
			System.out.println(response.toString());
		}

		JSONObject jObject = new JSONObject(response.toString());
		accessToken = jObject.getString("access_token");

		return accessToken;
	}
}
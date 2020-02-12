package com.example.vizassist.utilities;

import android.graphics.Bitmap;

/*import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;*/
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utility class with methods to perform http operations
 */
public class HttpUtilities {

    /**
     * Make a {@link HttpURLConnection} that can be used to send a POST request with image data.
     *
     * @param bitmap    image to be sent to server
     * @param urlString URL address of OCR server
     * @return {@link HttpURLConnection} to be used to connect to server
     * @throws IOException
     */
    public static HttpURLConnection makeHttpPostConnectionToUploadImage(Bitmap bitmap,
                                                                        String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        byte[] data = bos.toByteArray();

        //ByteArrayEntity byteArrayEntity = new ByteArrayEntity(data, ContentType.IMAGE_JPEG);
        MyEntity byteArrayEntity = new MyEntity(data, ContentType.IMAGE_JPEG);

        conn.addRequestProperty("Content-length", byteArrayEntity.getContentLength() + "");
        conn.addRequestProperty(byteArrayEntity.getContentType().getName(), byteArrayEntity.getContentType().getValue());

        //OutputStream os = conn.getOutputStream();
        byteArrayEntity.writeTo(conn.getOutputStream());
        //os.close();

        return conn;
    }

    public static HttpURLConnection makeHttpPostConnectionToUploadImage2(Bitmap bitmap,
                                                                        String urlString) throws IOException {

        String endString = "\r\n";
        String twoHyphen = "--";
        String boundary = "*****";
        String key = "userhead";

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "utf-8");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        byte[] imageBytes = bos.toByteArray();

        //设置DataOutputStream
        DataOutputStream dsDataOutputStream = new DataOutputStream(conn.getOutputStream());
        dsDataOutputStream.writeBytes(twoHyphen + boundary + endString);
        dsDataOutputStream.writeBytes("Content-Disposition:form-data;" + "name=\"" + key + "\";filename=\"" +
                "11.jpg\"" + endString);
        //dsDataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\";filename=\"" +
        //        "11.jpg"+ "\"" + endString);
        dsDataOutputStream.writeBytes(endString);

        //取得文件的FileInputStream
        dsDataOutputStream.write(imageBytes, 0, imageBytes.length);
        //dsDataOutputStream.write(imageBytes);

        dsDataOutputStream.writeBytes(endString);
        dsDataOutputStream.writeBytes(twoHyphen + boundary + twoHyphen + endString);

        dsDataOutputStream.flush();
        dsDataOutputStream.close();

        return conn;
    }

    public static HttpURLConnection makeHttpPostConnectionToUploadImage3(Bitmap bitmap,
                                                                         String urlString) throws IOException {

        String filename = "filename.jpg";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        ContentBody contentPart = new ByteArrayBody(bos.toByteArray(), filename);
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("picture", contentPart);

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.addRequestProperty("Content-length", reqEntity.getContentLength()+"");
        conn.addRequestProperty(reqEntity.getContentType().getName(), reqEntity.getContentType().getValue());

        OutputStream os = conn.getOutputStream();
        reqEntity.writeTo(conn.getOutputStream());
        os.close();

        return conn;
    }
    /**
     * Parse OCR response return by OCR server.
     *
     * @param httpURLConnection @{@link HttpURLConnection} used to connect to OCR server, which
     *                          contains a response JSON if succeeded.
     * @return a string representing text found in the image sent to OCR server
     * @throws JSONException
     * @throws IOException
     */
    public static String parseOCRResponse(HttpURLConnection httpURLConnection) throws JSONException,
            IOException {
        JSONObject resultObject = new JSONObject(readStream(httpURLConnection.getInputStream()));
        String result = resultObject.getString("text");
        return result;
    }

    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }
}

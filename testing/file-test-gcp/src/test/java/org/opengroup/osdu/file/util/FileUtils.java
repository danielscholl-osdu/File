package org.opengroup.osdu.file.util;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import org.springframework.util.StreamUtils;

public class FileUtils {

  public String readFromLocalFilePath(String filePath) throws IOException {

    InputStream inStream = this.getClass().getResourceAsStream(filePath);
    BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
    StringBuilder stringBuilder = new StringBuilder();

    String eachLine = "";
    while ((eachLine = br.readLine()) != null) {
      stringBuilder.append(eachLine);
    }

    return stringBuilder.toString();
  }

  public static boolean isNullOrEmpty(final Collection<?> c) {
    return c == null || c.isEmpty();
  }

  public int uploadFileBySignedUrl(String endPoint, String inputFilePath) throws IOException {
    String fileContent = readFromLocalFilePath(inputFilePath);

    OkHttpClient client = new OkHttpClient();
    MediaType mediaType = MediaType.parse("text/csv");
    RequestBody body = RequestBody.create(mediaType, fileContent);

    Request request = new Request.Builder().url(endPoint).method("PUT", body)
        .addHeader("Content-Type", "text/csv")
        .addHeader("x-ms-blob-type", "BlockBlob").build();
    Response response = client.newCall(request).execute();

    return response.code();
  }

  public String readFileBySignedUrl(URL fileURL) throws IOException {
    URLConnection conn = fileURL.openConnection();
    return StreamUtils.copyToString(conn.getInputStream(), StandardCharsets.UTF_8);
  }

  public void readFileBySignedUrlAndWriteToLocalFile(String fileURL, String outputFilePath) throws InterruptedException,
      AWTException {
    URL url;
    try {
      url = new URL(fileURL);
      URLConnection conn = url.openConnection();
      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String inputLine;
      String fileName = System.getProperty("user.dir") + outputFilePath;
      File file = new File(fileName);
      if (!file.exists()) {
        file.createNewFile();
      }
      FileWriter fw = new FileWriter(file.getAbsoluteFile());
      BufferedWriter bw = new BufferedWriter(fw);
      while ((inputLine = br.readLine()) != null) {
        bw.write(inputLine);
      }
      bw.close();
      br.close();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}

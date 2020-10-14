package com.cropdox.utilitarios;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.cropdox.CameraActivity;

import org.opencv.core.Scalar;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RemoteUploader {

    private static final String ENDERECO = "http://127.0.0.1/receber-arquivo";
    private URL url;
    private HttpURLConnection con;
    private String delimiter = "--";
    private String boundary =  "SwA" + Long.toString(System.currentTimeMillis()) + "SwA";
    private OutputStream os;
    private CharArrayWriter buffer = null;

    public RemoteUploader(String endereco) throws Exception {
        url = new URL(endereco);
        con = (HttpURLConnection) url.openConnection();
        //this.connectForMultipart();
    }
    public void connectForMultipart() throws Exception {
        //con = (HttpURLConnection) ( new URL(url)).openConnection();
        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        con.connect();
        os = con.getOutputStream();
    }
    public void addFormPart(String paramName, String value) throws Exception {
        writeParamData(paramName, value);
    }

    private void writeParamData(String paramName, String value) throws Exception {
        os.write( (delimiter + boundary + "\r\n").getBytes());
        os.write( "Content-Type: text/plain\r\n".getBytes());
        os.write( ("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n").getBytes());;
        os.write( ("\r\n" + value + "\r\n").getBytes());

    }
    public void addFilePart(String paramName, String fileName, byte[] data) throws Exception {
        os.write( (delimiter + boundary + "\r\n").getBytes());
        os.write( ("Content-Disposition: form-data; name=\"" + paramName +  "\"; filename=\"" + fileName + "\"\r\n"  ).getBytes());
        os.write( ("Content-Type: application/octet-stream\r\n"  ).getBytes());
        os.write( ("Content-Transfer-Encoding: binary\r\n"  ).getBytes());
        os.write("\r\n".getBytes());
        os.write(data);
        os.write("\r\n".getBytes());
    }


    public void finishMultipart() throws Exception {
        os.write( (delimiter + boundary + delimiter + "\r\n").getBytes());
    }

    public String getResponse() throws IOException {
        InputStream is = con.getInputStream();
        byte[] b = new byte[1024];
        while ( is.read(b) != -1)
            buffer.append(new String(b));
        con.disconnect();
        return buffer.toString();

    }
}

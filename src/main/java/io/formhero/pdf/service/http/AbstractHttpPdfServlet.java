package io.formhero.pdf.service.http;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by ryan.kimber on 2017-12-10.
 */
public abstract class AbstractHttpPdfServlet extends HttpServlet {

    protected String getRequestBody(HttpServletRequest request) throws IOException
    {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            }
            else {
                stringBuilder.append("");
            }
        }
        catch (IOException ex) {
            throw ex;
        }
        finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                }
                catch (IOException ex) {
                    throw ex;
                }
            }
        }
        return stringBuilder.toString();
    }
}

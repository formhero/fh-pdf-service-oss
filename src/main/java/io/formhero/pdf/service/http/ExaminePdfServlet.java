package io.formhero.pdf.service.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class ExaminePdfServlet  extends AbstractHttpPdfServlet
{
  private static final Logger log = LogManager.getLogger(ExaminePdfServlet.class.getName());
  private ObjectMapper mapper = new ObjectMapper();

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {

  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    resp.setContentType("text/plain");
    resp.getWriter().write("Hello, from the PDF Examine Servlet!");
    resp.getWriter().close();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    HttpExaminePdfDataRequest examineDataRequest = null;
    try {
      String requestBody = getRequestBody(req);
      HttpPdfRequest serviceRequest = mapper.readValue(requestBody, HttpPdfRequest.class);

      // Convert to specific request object
      examineDataRequest = (HttpExaminePdfDataRequest) serviceRequest;
      log.info("Request received to examine PDF of size: " + examineDataRequest.getPdfBytes().length);

      Map<String, String> formData = examineDataRequest.getFormDataJson();
      String formDataJson = mapper.writeValueAsString(formData);
      log.info("Examined PDF: " + formDataJson);
      resp.setHeader("Content-Type", "application/json;charset=UTF-8");
      resp.getWriter().write(formDataJson);
      resp.getWriter().close();
    }
    catch (Throwable t) {
      if (examineDataRequest != null) {
        log.info("Examine PDF failed.", t);
      }
      else {
        log.error("Unable to service request due to exception: " + t.getMessage(), t);
      }
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while examining the PDF artifact:" + t.getMessage());
    }
  }
}

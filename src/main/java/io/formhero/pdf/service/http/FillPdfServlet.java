package io.formhero.pdf.service.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by ryan.kimber on 2017-12-08.
 */
public class FillPdfServlet extends AbstractHttpPdfServlet
{
  private static final Logger log = LogManager.getLogger(FillPdfServlet.class.getName());
  private ObjectMapper mapper = new ObjectMapper();

  @Override
  public void init(ServletConfig servletConfig) throws ServletException
  {

  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    resp.setContentType("text/plain");
    resp.getWriter().write("Hello, from the PDF Refinery Fill Servlet!");
    resp.getWriter().close();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    HttpMergePdfDataRequest mergeDataRequest = null;
    try {
      String requestBody = getRequestBody(req);
      HttpPdfRequest serviceRequest = mapper.readValue(requestBody, HttpPdfRequest.class);

      if (!HttpPdfRequest.MERGE_PDF_DATA.equals(serviceRequest.getType())) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported Request Type");
      }
      else {
        mergeDataRequest = (HttpMergePdfDataRequest) serviceRequest;
        log.info("Request received to build " + mergeDataRequest.getBucket() +
            "/" + mergeDataRequest.getFileFolder() + "/" + mergeDataRequest.getFileName() +
            " for session " + mergeDataRequest.getSessionId());
        byte[] outputBytes = mergeDataRequest.getResultingPdf();
        resp.setContentType("application/pdf");
        resp.getOutputStream().write(outputBytes);
        resp.getOutputStream().close();
        log.info("Build of " + mergeDataRequest.getBucket() + "/" + mergeDataRequest.getFileFolder() +
            "/" + mergeDataRequest.getFileName() + " for session " + mergeDataRequest.getSessionId() + " completed.");
      }
    }
    catch (Throwable t) {
      if (mergeDataRequest != null) {
        log.info("Build of " + mergeDataRequest.getBucket() + "/" + mergeDataRequest.getFileFolder() +
            "/" + mergeDataRequest.getFileName() + " for session " + mergeDataRequest.getSessionId() + " failed.", t);
      }
      else {
        log.error("Unable to service request due to exception: " + t.getMessage(), t);
      }
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while generating the PDF artifact:" + t.getMessage());
    }
  }
}

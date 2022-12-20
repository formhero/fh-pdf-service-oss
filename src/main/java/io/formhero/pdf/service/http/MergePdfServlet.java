package io.formhero.pdf.service.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.formhero.pdf.PdfServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by ryan.kimber on 2017-12-09.
 */
public class MergePdfServlet extends AbstractHttpPdfServlet
{
    private static final Logger log = LogManager.getLogger(MergePdfServlet.class.getName());
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init(ServletConfig servletConfig) throws ServletException
    {

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/plain");
        resp.getWriter().write("Hello, from the PDF Refinery Merge Servlet!");
        resp.getWriter().close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String requestBody = getRequestBody(req);
        HttpPdfRequest serviceRequest = mapper.readValue(requestBody, HttpPdfRequest.class);

        if (!HttpPdfRequest.MERGE_PDFS.equals(serviceRequest.getType())) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported Request Type");
        }
        else {
            HttpMergePdfsRequest mergePdfsRequest = (HttpMergePdfsRequest) serviceRequest;
            try {
                log.info("Request received to merge PDFs together for session " + mergePdfsRequest.getSessionId());
                byte[] outputBytes = mergePdfsRequest.getResultingPdf();
                resp.setContentType("application/pdf");
                resp.getOutputStream().write(outputBytes);
                resp.getOutputStream().close();
                log.info("Completed merge of PDFs for session " + mergePdfsRequest.getSessionId());
            }
            catch (PdfServiceException e) {
                log.error("Error while merging PDFs: " + e.getMessage(), e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while generating the PDF artifact: " + e.getMessage());
            }
        }
    }
}

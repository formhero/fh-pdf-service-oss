package io.formhero.pdf;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopyFields;
import com.itextpdf.text.pdf.PdfReader;

import java.io.*;
import java.lang.reflect.Field;


class PasswordRemover {
	/**
     * Set the value of a potentially private boolean field on an object.
     *
     * @param o the Object on which to set the field
     * @param fieldName the name of the field to set
     * @param value the new value to set the field to
     * @throws NoSuchFieldException if <code>fieldName</code> is not the name of a field in the class of <code>o</code>
     */
    private static void setBooleanField(Object o, String fieldName, boolean value) throws NoSuchFieldException {
        Field field = o.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        try {
            field.setBoolean(o, value);
        } catch (IllegalAccessException iae) {
            // The call to setAccessible() should have stopped this from happening.  If it didn't then we are probably running in
            // some more strict container or virtual machine.
            throw new RuntimeException(iae);
        }
    }

    /**
     * Removes the owner password on a PDF document passed through as a stream.
     *
     * @return The PDF provided in the input stream, with password protection removed.
     * @param input the InputStream containing the document to liberate
     * @throws IOException if there is a problem reading from or writing to the supplied streams
     * @throws DocumentException if there is a problem parsing or writing the PDF document
     */
    public static byte[] removeRestrictions(InputStream input) throws IOException, DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	PdfReader reader = new PdfReader(input);
        try {
            setBooleanField(reader, "ownerPasswordUsed", false);
            setBooleanField(reader, "encrypted", false);
        } catch (NoSuchFieldException nsfe) {
            // We expect these fields to be part of iText.  If they are not found, then we are probably using a different version.
            AssertionError ae = new AssertionError("could not find a field");
            ae.initCause(nsfe);
            throw ae;
        }

        reader.removeUsageRights();
        PdfCopyFields copy = new PdfCopyFields(baos);
        copy.addDocument(reader);
        copy.close();
        return baos.toByteArray();
    }
    
    public static byte[] removePasswordAndRestrictions(InputStream input, String currentPassword) throws IOException, DocumentException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	PdfReader reader = new PdfReader(input, currentPassword.getBytes());
        try {
            setBooleanField(reader, "ownerPasswordUsed", false);
            setBooleanField(reader, "encrypted", false);
        } catch (NoSuchFieldException nsfe) {
            // We expect these fields to be part of iText.  If they are not found, then we are probably using a different version.
            AssertionError ae = new AssertionError("could not find a field");
            ae.initCause(nsfe);
            throw ae;
        }

        reader.removeUsageRights();
        PdfCopyFields copy = new PdfCopyFields(baos);
        copy.addDocument(reader);
        copy.close();
        return baos.toByteArray();
    }


    public static void main(String[] args)
    {
        try {
            File pdfFile = new File(args[0]);
            FileInputStream fis = new FileInputStream(pdfFile);


            FileOutputStream fos = new FileOutputStream(args[0].substring(0, args[0].lastIndexOf('.')) + ".unlocked.pdf");
            //fos.write(removePasswordAndRestrictions(fis));

        }
        catch(IOException ioe)
        {

        }
    }
}

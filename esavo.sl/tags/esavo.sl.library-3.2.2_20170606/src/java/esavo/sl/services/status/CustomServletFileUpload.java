/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package esavo.sl.services.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.fileupload.util.Streams;

import esavo.sl.services.upload.UploadProgressListener;

public class CustomServletFileUpload extends ServletFileUpload {
	
	public static final String PARAM_TASKID = "TASKID";

	public CustomServletFileUpload(FileItemFactory fac){
		super(fac);
	}
	
    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream.
     *
     * @param request The servlet request to be parsed.
     *
     * @return A list of <code>FileItem</code> instances parsed from the
     *         request, in the order that they were transmitted.
     *
     * @throws FileUploadException if there are problems reading/parsing
     *                             the request or storing files.
     */
    @Override
    public List<FileItem> parseRequest(HttpServletRequest request)
    throws FileUploadException {
        return parseRequest(new ServletRequestContext(request));
    }
    
    
    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream.
     *
     * @param ctx The context for the request to be parsed.
     *
     * @return A list of <code>FileItem</code> instances parsed from the
     *         request, in the order that they were transmitted.
     *
     * @throws FileUploadException if there are problems reading/parsing
     *                             the request or storing files.
     */
    public List<FileItem> parseRequest(RequestContext ctx)
            throws FileUploadException {
    	UploadProgressListener progressListener = (UploadProgressListener)getProgressListener();
    	
    	//debug
//    	try{
//    		InputStream is = ctx.getInputStream();
//    		int r;
//    		while((r = is.read()) != -1){
//    			if(r == 0x0A){
//    				System.out.println("");
//    			}else{
//    				System.out.print((char)r);
//    			}
//    		}
//    	}catch(IOException ioe){
//    		throw new RuntimeException(ioe);
//    	}
    	
        List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx);
            FileItemFactory fac = getFileItemFactory();
            if (fac == null) {
                throw new NullPointerException("No FileItemFactory has been set.");
            }
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                // Don't use getName() here to prevent an InvalidFileNameException.
                final String fileName = item.getName();
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(),
                                                   item.isFormField(), fileName);
                items.add(fileItem);
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true);
                } catch (FileUploadIOException e) {
                    throw (FileUploadException) e.getCause();
                } catch (IOException e) {
                    throw new IOFileUploadException(String.format("Processing of %s request failed. %s",
                                                           MULTIPART_FORM_DATA, e.getMessage()), e);
                }
                
                if(fileItem.getFieldName().equals(PARAM_TASKID)){
                	progressListener.setTaskId(Long.parseLong(fileItem.getString()));
                }
                
                //final FileItemHeaders fih = item.getHeaders();
                //fileItem.setHeaders(fih);
            }
            successful = true;
            return items;
        } catch (FileUploadIOException e) {
            throw (FileUploadException) e.getCause();
        } catch (IOException e) {
            throw new FileUploadException(e.getMessage(), e);
        } finally {
            if (!successful) {
                for (FileItem fileItem : items) {
                    try {
                        fileItem.delete();
                    } catch (Throwable e) {
                        // ignore it
                    }
                }
            }
        }
    }

	
	

}

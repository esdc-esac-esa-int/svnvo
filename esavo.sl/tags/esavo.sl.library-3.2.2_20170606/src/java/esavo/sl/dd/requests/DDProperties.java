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
package esavo.sl.dd.requests;

public interface DDProperties {
	
	public static final String CONTENT_TYPE_TEXT_XML = "text/xml";
	public static final String CONTENT_TYPE_FITS = "image/fits";
	public static final String CONTENT_TYPE_IMAGE_JPG = "image/jpeg";
	public static final String CONTENT_TYPE_IMAGE_GIF = "image/gif";
	public static final String CONTENT_TYPE_BINARY = "application/octet-stream";
	public static final String CONTENT_TYPE_PDF = "application/pdf";
	
	public static final boolean COMPRESS_FILE = true;
	public static final boolean DO_NOT_COMPRESS_FILE = false;
	
	public static final String GEO_IP_DAT_RESOURCE_NAME = "dd.geo_ip_dat.resource_name";
	public static final String GEO_IP_DAT_DST_DIR = "dd.geo_ip_dat.dst_dir";


   /**
    * We will return file paths from linux/unix systems, so the separator will
    * always be this one.
    */
   public static String LINUX_FILE_SEPARATOR = "/";

   /**
    * Public proprietary status.
    */
   public static int PROPRIETARY_STATUS_PUBLIC = 0;

   /**
    * Public proprietary status.
    */
   public static int PROPRIETARY_STATUS_PRIVATE = 1;

   /**
    * Successful data distribution processing.
    */
   public static int PROCESS_OK = 0;

   /**
    * Unsucessful data distribution processing.
    */
   public static int PROBLEMS_IN_PROCESS = -1;
   
   public static int PROCESS_NO_PRODUCTS = 1;
   

	public static final String PARAM_ID_RETRIEVAL_ACCESS = "retrieval_access";
	public static final String PARAM_ID_RETRIEVAL_TYPE = "retrieval_type";
	public static final String PARAM_ID_RA_IDS_FORMAT = "retrieval_access_ids_format";
	
	/**
	 * By default: FALSE. In case of one file, no tar file is created.
	 */
	public static final String PARAM_ID_FORCE_TAR = "forcetar";
	
	/**
	 * By default FALSE. Force decompression
	 */
	public static final String PARAM_ID_UNCOMPRESS = "uncompress";
	/**
	 * By default FALSE: Force compression
	 */
	public static final String PARAM_ID_COMPRESS = "compress";

	public static final String RETRIEVAL_TYPE_POSTCARD = "POSTCARD";
	public static final String RETRIEVAL_TYPE_PRODUCT = "PRODUCT";
	public static final String RETRIEVAL_TYPE_OBSERVATION = "OBSERVATION";

	public static final String PROP_COMPRESS_SINGLE_FILE = "dd.compress_single_file";
	public static final String PROP_EMAIL_CONF = "dd.email_conf";
	public static final String PROP_FTP_HOST = "dd.ftp_host";
	public static final String PROP_FTP_SECURE = "dd.ftp_secure";
	public static final String PROP_FTP_PUBLIC = "dd.ftp_public";
	public static final String PROP_FTP_PROTOCOL = "dd.ftp_protocol";
	public static final String PROP_FTP_PORT = "dd.ftp_port";
	public static final String PROP_HELPDESK_CUSTODIAN = "dd.helpdesk_custodian";
	public static final String PROP_MAIL_CUSTODIAN = "dd.mail_custodian";
	public static final String PROP_MAIL_FROM = "dd.mail_from";
	public static final String PROP_MASTER_EMAIL = "dd.master_email";
	public static final String PROP_MISSION_NAME = "dd.mission_name";
	public static final String PROP_PRIVATE_DATA_URL = "dd.private_data_url";
	public static final String PROP_PUBLIC_DATA_URL = "dd.public_data_url";
	public static final String PROP_REPO_TOP_LEVEL = "dd.repo_top_level";
	public static final String PROP_SMTP_HOST = "dd.smtp_host";
	public static final String PROP_FILE_PATH_DEBUG = "dd.file_path_debug";
	


}

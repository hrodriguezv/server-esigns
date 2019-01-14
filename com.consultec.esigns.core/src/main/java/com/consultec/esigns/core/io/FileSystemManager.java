/**
 * 
 */

package com.consultec.esigns.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.consultec.esigns.core.util.PropertiesManager;

/**
 * The Class FileSystemManager.
 *
 * @author hrodriguez
 */
public class FileSystemManager {

	/** The Constant IMAGE_SRC_EXT. */
	private static final String IMAGE_SRC_EXT =
		PropertiesManager.getInstance().getValue(
			PropertiesManager.PROPERTY_USER_STROKE_IMGEXT);

	/** The Constant TEXT_SRC_EXT. */
	private static final String TEXT_SRC_EXT =
		PropertiesManager.getInstance().getValue(
			PropertiesManager.PROPERTY_USER_STROKE_TEXTEXT);

	/** The user home. */
	private File userHome;

	/** The pdf document. */
	private File pdfDocument;

	/** The pdf stroked doc. */
	private File pdfStrokedDoc;

	/** The pdf esigned doc. */
	private File pdfEsignedDoc;

	/** The pdf e-signed stamped doc. */
	private File pdfEsignedStampedDoc;

	/** The certficate. */
	private File certificate;

	/** The image sign files. */
	private List<File> imgStrokeFiles;

	/** The text sign files. */
	private List<File> textStrokeFiles;

	/** The mutex. */
	private static Object mutex = new Object();

	/** The instance. */
	private static FileSystemManager instance;

	/** The session id. */
	private String sessionId;

	/** The serialized object ref. */
	private File serializedObjectRef;

	/**
	 * Instantiates a new file system manager.
	 */
	private FileSystemManager() {

	}

	/**
	 * File exists.
	 *
	 * @param file
	 *            the file
	 * @return true, if successful
	 */
	private boolean fileExists(File file) {

		return file != null && file.exists();
	}

	/**
	 * Delete file.
	 *
	 * @param file
	 *            the file
	 * @return true, if successful
	 */
	private boolean deleteFile(File file) {

		if (fileExists(file)) {
			return file.delete();
		}

		return false;
	}

	/**
	 * Gets the single instance of FileSystemManager.
	 *
	 * @return single instance of FileSystemManager
	 */
	public static FileSystemManager getInstance() {

		FileSystemManager result = instance;
		if (result == null) {
			synchronized (mutex) {
				result = instance;
				if (result == null)
					instance = result = new FileSystemManager();
			}
		}
		return result;
	}

	/**
	 * Creates the local workspace.
	 *
	 * @param sessionId
	 *            the session id
	 * @param contentFile
	 *            the content file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void createLocalWorkspace(String sessionId, String contentFile)
		throws IOException {

		if (instance.userHome.exists()) {
			throw new FileAlreadyExistsException(
				"Another instance is already running");
		}

		// create local workspace
		// write the received base64 package
		String path = PropertiesManager.getInstance().getValue(
			PropertiesManager.PROPERTY_USER_BASE_HOME);
		String plainFileName = PropertiesManager.getInstance().getValue(
			PropertiesManager.PROPERTY_USER_HOME_PDFDOCUMENT);
		String file = path + "/" + sessionId + "/" + plainFileName;

		IOUtility.writeDecodedContent(file, contentFile);
	}

	/**
	 * Inits and check consistency of local workspace.
	 *
	 * @param id
	 *            the str
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	public void init(String id)
		throws FileNotFoundException {

		PropertiesManager pref = PropertiesManager.getInstance();

		String pathHome =
			pref.getValue(PropertiesManager.PROPERTY_USER_BASE_HOME);

		File homeDir = new File(pathHome);
		instance.sessionId = id;
		instance.userHome = new File(homeDir, id);

		instance.certificate = new File(
			pref.getValue(PropertiesManager.PROPERTY_OPERATOR_CERTIFICATE));

		instance.pdfDocument = new File(
			instance.userHome,
			pref.getValue(PropertiesManager.PROPERTY_USER_HOME_PDFDOCUMENT));

		instance.pdfStrokedDoc = new File(
			instance.userHome,
			pref.getValue(PropertiesManager.PROPERTY_USER_HOME_STROKEDOCUMENT));

		instance.pdfEsignedDoc = new File(
			instance.userHome, pref.getValue(
				PropertiesManager.PROPERTY_USER_HOME_ESIGNEDDOCUMENT));

		instance.imgStrokeFiles = new ArrayList<>();
		instance.textStrokeFiles = new ArrayList<>();
		instance.serializedObjectRef = new File(instance.userHome, "ref.ser");
	}

	/**
	 * Check consistency.
	 *
	 * @param sessionId
	 *            the session id
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void checkConsistency(String sessionId)
		throws IOException {

		if (!instance.sessionId.equals(sessionId))
			throw new IllegalStateException(
				"Inconsistency in FileSystem != sessionId");

		if (!instance.userHome.exists()) {
			throw new FileNotFoundException("User home folder doesn't exist!");
		}

		instance.certificate = new File(
			PropertiesManager.getInstance().getValue(
				PropertiesManager.PROPERTY_OPERATOR_CERTIFICATE));

		instance.pdfDocument = new File(
			instance.userHome, PropertiesManager.getInstance().getValue(
				PropertiesManager.PROPERTY_USER_HOME_PDFDOCUMENT));

		if (!instance.pdfDocument.exists()) {
			throw new FileNotFoundException(
				"PDF Document to sign doesn't exist!");
		}

		if (!instance.pdfStrokedDoc.exists()) {
			throw new FileNotFoundException(
				"PDF Document signed doesn't exist!");
		}

		File[] images = instance.userHome.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {

				return name.toLowerCase().endsWith(IMAGE_SRC_EXT);
			}
		});
		for (File file : images) {
			instance.addImgStrokeFile(file);
		}

		File[] strokes = instance.userHome.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {

				return name.toLowerCase().endsWith(TEXT_SRC_EXT);
			}
		});
		for (File file : strokes) {
			instance.addTextStrokeFile(file);
		}
	}

	/**
	 * Delete on exit.
	 *
	 * @param doIt
	 *            the do it
	 */
	public void deleteOnExit(Boolean doIt) {

		if (doIt) {
			deleteFile(instance.pdfDocument);
			deleteFile(instance.pdfEsignedDoc);
			deleteFile(instance.pdfEsignedStampedDoc);
			deleteFile(instance.pdfStrokedDoc);
			deleteFile(instance.serializedObjectRef);

			for (File file : imgStrokeFiles) {
				deleteFile(file);
			}

			for (File file : textStrokeFiles) {
				deleteFile(file);
			}
			deleteFile(instance.userHome);
		}
	}

	/**
	 * Serialize object file.
	 *
	 * @param ref
	 *            the ref
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void serializeObjectFile(Object ref)
		throws IOException {

		FileUtils.writeByteArrayToFile(
			instance.serializedObjectRef, StreamHelper.toStream(ref));
	}

	/**
	 * Deserialize object.
	 *
	 * @return the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Object deserializeObject()
		throws IOException {

		return StreamHelper.fromStream(
			FileUtils.readFileToByteArray(instance.serializedObjectRef));
	}

	/**
	 * Gets the pdf document.
	 *
	 * @return the pdf document
	 */
	public File getPdfDocument() {

		return instance.pdfDocument;
	}

	/**
	 * Adds the img signature file.
	 *
	 * @param e
	 *            the e
	 */
	public void addImgStrokeFile(File e) {

		instance.imgStrokeFiles.add(e);
	}

	/**
	 * Adds the text signature file.
	 *
	 * @param e
	 *            the e
	 */
	public void addTextStrokeFile(File e) {

		instance.textStrokeFiles.add(e);
	}

	/**
	 * Gets the base home.
	 *
	 * @return the base home
	 */
	public File getBaseHome() {

		return instance.userHome;
	}

	/**
	 * Gets the pdf stroked doc.
	 *
	 * @return the pdf stroked doc
	 */
	public File getPdfStrokedDoc() {

		return pdfStrokedDoc;
	}

	/**
	 * Sets the pdf stroked doc.
	 *
	 * @param pdfStrokedDoc
	 *            the new pdf stroked doc
	 */
	public void setPdfStrokedDoc(File pdfStrokedDoc) {

		this.pdfStrokedDoc = pdfStrokedDoc;
	}

	/**
	 * Gets the session id.
	 *
	 * @return the session id
	 */
	public String getSessionId() {

		return instance.sessionId;
	}

	/**
	 * Gets the image stroke files.
	 *
	 * @return the image stroke files
	 */
	public List<File> getImageStrokeFiles() {

		return imgStrokeFiles;
	}

	/**
	 * Gets the text stroke files.
	 *
	 * @return the text stroke files
	 */
	public List<File> getTextStrokeFiles() {

		return textStrokeFiles;
	}

	/**
	 * Gets the certificate.
	 *
	 * @return the certificate
	 */
	public File getCertificate() {

		return instance.certificate;
	}

	/**
	 * Gets the pdf esigned doc.
	 *
	 * @return the pdf esigned doc
	 */
	public File getPdfEsignedDoc() {

		return pdfEsignedDoc;
	}

	/**
	 * Gets the serialized object ref.
	 *
	 * @return the serialized object ref
	 */
	public File getSerializedObjectRef() {

		return serializedObjectRef;
	}
}

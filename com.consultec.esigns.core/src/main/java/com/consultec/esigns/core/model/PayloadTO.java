/**
 * 
 */

package com.consultec.esigns.core.model;

/**
 * The Class Post.
 *
 * @author hrodriguez
 */
public class PayloadTO {

	/**
	 * The Enum Stage.
	 */
	public enum Stage {

			/** The init stage. */
			INIT,
			/** The manual signed. */
			MANUAL_SIGNED,
			/** The e signed. */
			E_SIGNED,
			/** The completed. */
			COMPLETED
	}

	/**
	 * Instantiates a new post.
	 */
	public PayloadTO() {

	}

	/** The stage. */
	private Stage stage;

	/** The plain doc encoded. */
	private String plainDocEncoded;

	/** The stroked doc encoded. */
	private String strokedDocEncoded;

	/** The signed doc encoded. */
	private String signedDocEncoded;

	/** The strokes. */
	private String[] strokes;

	/** The images. */
	private String[] images;

	/** The session ID. */
	private String sessionID;

	/** The user logged. */
	private String userLogged;

	/**
	 * Gets the stage.
	 *
	 * @return the stage
	 */
	public Stage getStage() {

		return stage;
	}

	/**
	 * Sets the stage.
	 *
	 * @param stage
	 *            the new stage
	 */
	public void setStage(Stage stage) {

		this.stage = stage;
	}

	/**
	 * Gets the plain doc encoded.
	 *
	 * @return the plain doc encoded
	 */
	public String getPlainDocEncoded() {

		return plainDocEncoded;
	}

	/**
	 * Sets the plain doc encoded.
	 *
	 * @param plainDocEncoded
	 *            the new plain doc encoded
	 */
	public void setPlainDocEncoded(String plainDocEncoded) {

		this.plainDocEncoded = plainDocEncoded;
	}

	/**
	 * Gets the stroked doc encoded.
	 *
	 * @return the stroked doc encoded
	 */
	public String getStrokedDocEncoded() {

		return strokedDocEncoded;
	}

	/**
	 * Sets the stroked doc encoded.
	 *
	 * @param strokedDocEncoded
	 *            the new stroked doc encoded
	 */
	public void setStrokedDocEncoded(String strokedDocEncoded) {

		this.strokedDocEncoded = strokedDocEncoded;
	}

	/**
	 * Gets the signed doc encoded.
	 *
	 * @return the signed doc encoded
	 */
	public String getSignedDocEncoded() {

		return signedDocEncoded;
	}

	/**
	 * Sets the signed doc encoded.
	 *
	 * @param signedDocEncoded
	 *            the new signed doc encoded
	 */
	public void setSignedDocEncoded(String signedDocEncoded) {

		this.signedDocEncoded = signedDocEncoded;
	}

	/**
	 * Gets the strokes.
	 *
	 * @return the strokes
	 */
	public String[] getStrokes() {

		return strokes;
	}

	/**
	 * Sets the strokes.
	 *
	 * @param strokes
	 *            the new strokes
	 */
	public void setStrokes(String[] strokes) {

		this.strokes = strokes;
	}

	/**
	 * Gets the session ID.
	 *
	 * @return the session ID
	 */
	public String getSessionID() {

		return sessionID;
	}

	/**
	 * Sets the session ID.
	 *
	 * @param sessionID
	 *            the new session ID
	 */
	public void setSessionID(String sessionID) {

		this.sessionID = sessionID;
	}

	/**
	 * Gets the images.
	 *
	 * @return the images
	 */
	public String[] getImages() {

		return images;
	}

	/**
	 * Sets the images.
	 *
	 * @param images
	 *            the new images
	 */
	public void setImages(String[] images) {

		this.images = images;
	}

	/**
	 * Gets the user logged.
	 *
	 * @return the user logged
	 */
	public String getUserLogged() {

		return userLogged;
	}

	/**
	 * Sets the user logged.
	 *
	 * @param userLogged
	 *            the new user logged
	 */
	public void setUserLogged(String userLogged) {

		this.userLogged = userLogged;
	}

}

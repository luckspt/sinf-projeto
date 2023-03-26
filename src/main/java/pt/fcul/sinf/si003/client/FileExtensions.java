package pt.fcul.sinf.si003.client;

/**
 * File extensions used in the application.
 */
public enum FileExtensions {
    /**
     * The file extension of the encrypted file.
     */
    CIFRADO("cifrado"),
    /**
     * The file extension of the signed file.
     */
    ASSINADO("assinado"),
    /**
     * The file extension of the encrypted and signed file.
     */
    SEGURO("seguro"),
    /**
     * The file extension of the wrapped symmetric key.
     */
    CHAVE_SECRETA("chave_secreta"),
    /**
     * The file extension of the signature.
     */
    ASSINATURA("assinatura");

    /**
     * The file extension.
     */
    private final String extension;

    /**
     * Creates a new instance of FileExtensions.
     *
     * @param extension The file extension.
     */
    FileExtensions(String extension) {
        this.extension = extension;
    }

    /**
     * Gets the file extension.
     *
     * @return The file extension.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Gets the file extension with a dot.
     *
     * @return The file extension with a dot.
     */
    public String getExtensionWithDot() {
        return "." + extension;
    }
}

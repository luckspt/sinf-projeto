package pt.fcul.sinf.si003.client;

public enum FileExtensions {
    CIFRADO("cifrado"),
    ASSINADO("assinado"),
    SEGURO("seguro");

    private final String extension;

    FileExtensions(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public String getExtensionWithDot() {
        return "." + extension;
    }
}

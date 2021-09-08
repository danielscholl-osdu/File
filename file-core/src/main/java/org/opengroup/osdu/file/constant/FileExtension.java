package org.opengroup.osdu.file.constant;

public enum FileExtension {
    PDF("application/pdf"), 
    TIFF("image/tiff"), 
    TXT("text/plain"),
    HTML("text/html"), 
    PPT("application/vnd.ms-powerpoint"), 
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation"), 
    XLS("application/vnd.ms-excel"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), 
    DOC("application/msword"), 
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), 
    RTF("application/rtf"), 
    CHM("application/vnd.ms-htmlhelp"), 
    EML("message/rfc822"), 
    CSV("text/csv"),
    JPEG("image/jpeg"),
    PNG("image/png"),
    SVG("image/svg+xml"),
    JPG("image/jpg"),
    JSON("application/json"),
    MSG("application/vnd.ms-outlook");

    String mimeType;
    
    FileExtension(String mimeType) {
        this.mimeType = mimeType;
    }
    public String getMimeType() {
       return mimeType;
    } 
}

package org.opengroup.osdu.file.model.filemetadata.filedetails;

public enum ForKind {

    CRS("CRS"), Unit("Unit"), Measurement("Measurement"), AzimuthReference("AzimuthReference"), DateTime("DateTime");

    private String kind;

    ForKind(String kind)
    {
        this.kind = kind;
    }

    public String getValue()
    {
        return kind;
    }
}

package de.dhbw.woped.process2text.exception;

public interface BpmNlpHttpStatus {
    /** Error Status Code: standard exception occurs while generating the text model */
    public static final int CONVERTION_ERROR = 450;

    /** Error Status Code: RPST Convertion failed */
    public static final int RPST_FAILURE           = 451;

    /** Error Status Code: Failure wihile structuring the nodes of the RPST model */
    public static final int STRUCTURE_FAILURE      = 452;

    /** Error Status Code: Error while parsing the xml elements to the text model objects */
    public static final int PARSING_ERROR          = 453;

}

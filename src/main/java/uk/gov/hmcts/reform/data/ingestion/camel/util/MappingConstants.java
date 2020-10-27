package uk.gov.hmcts.reform.data.ingestion.camel.util;

public class MappingConstants {

    private MappingConstants() {
    }

    public static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    public static final String SCHEDULER_START_TIME = "start-time";

    public static final String ROUTE = "route";

    public static final String INSERT_SQL = "insert-sql";

    public static final String DELETE_SQL = "delete-sql";

    public static final String TRUNCATE_SQL = "truncate-sql";

    public static final String BLOBPATH = "blob-path";

    public static final String  PROCESSOR = "processor-class";

    public static final String  CSVBINDER = "csv-binder-object";

    public static final String  MAPPER = "mapper-class";

    public static final String  MAPPING_METHOD = "getMap";

    public static final String ID = "id";

    public static final String FILE_NAME = "file-name";

    public static final String TABLE_NAME = "table-name";

    public static final String HEADER_EXCEPTION = "header-exception";

    public static final String ROUTE_DETAILS = "routedetails";

    public static final String DIRECT_ROUTE = "direct:";

    public static final String IS_EXCEPTION_HANDLED = "is-exception-handled";

    public static final String SCHEDULER_STATUS = "SchedulerStatus";

    public static final String SCHEDULER_NAME = "SchedulerName";

    public static final String PARTIAL_SUCCESS = "PartialSuccess";

    public static final String FAILURE = "Failure";

    public static final String SUCCESS = "Success";

    public static final String NOT_STALE_FILE = "NotStale";

    public static final String PREVIOUS_DAY_FAILED = "PreviousDayFailed";

    public static final String ERROR_MESSAGE = "ErrorMessage";

    public static final String IS_FILE_STALE = "ISFILESTALE";

    public static final String FILENOTPRESENT_ERRORMESSAGE = "File not present";

    public static final String INVALID_JSR_PARENT = "Record skipped due to jsr violation in the record"
        .concat(" in the parent load");
}

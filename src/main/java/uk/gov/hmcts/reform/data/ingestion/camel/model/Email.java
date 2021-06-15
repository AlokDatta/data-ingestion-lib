package uk.gov.hmcts.reform.data.ingestion.camel.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class Email {
    private String subject;
    private String from;
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private String content;
}

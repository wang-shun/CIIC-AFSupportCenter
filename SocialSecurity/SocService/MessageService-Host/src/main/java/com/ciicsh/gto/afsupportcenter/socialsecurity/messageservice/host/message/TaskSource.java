package com.ciicsh.gto.afsupportcenter.socialsecurity.messageservice.host.message;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

/**
 * Created by houwanhua on 2018/2/24
 */
@Service
public interface TaskSource {
    String SOC_REPORT_OUTPUT = "soc_report_output_channel";

    @Output(SOC_REPORT_OUTPUT)
    MessageChannel socReportOutput();
}

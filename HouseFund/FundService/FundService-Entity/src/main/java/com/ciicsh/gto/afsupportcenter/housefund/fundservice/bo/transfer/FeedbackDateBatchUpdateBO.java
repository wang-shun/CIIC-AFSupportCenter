package com.ciicsh.gto.afsupportcenter.housefund.fundservice.bo.transfer;

import java.io.Serializable;
import java.time.LocalDate;

public class FeedbackDateBatchUpdateBO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long[] selectedData;
    private LocalDate feedbackDate;

    public Long[] getSelectedData() {
        return selectedData;
    }

    public void setSelectedData(Long[] selectedData) {
        this.selectedData = selectedData;
    }

    public LocalDate getFeedbackDate() {
        return feedbackDate;
    }

    public void setFeedbackDate(LocalDate feedbackDate) {
        this.feedbackDate = feedbackDate;
    }
}

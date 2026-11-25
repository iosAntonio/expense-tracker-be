package org.example.dto;

public class MonthDeletionResponseDTO {

    private String month;
    private int deletedCount;

    public MonthDeletionResponseDTO() {
    }

    public MonthDeletionResponseDTO(String month, int deletedCount) {
        this.month = month;
        this.deletedCount = deletedCount;
    }

    public String getMonth() {
        return month;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setDeletedCount(int deletedCount) {
        this.deletedCount = deletedCount;
    }
}

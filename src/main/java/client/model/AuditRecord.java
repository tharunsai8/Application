package client.model;

public class AuditRecord {
    private String changeMsg;
    private String changedBy;
    private String whenOccurred;

    public AuditRecord(String changeMsg, String changedBy, String whenOccurred) {
        this.changeMsg = changeMsg;
        this.changedBy = changedBy;
        this.whenOccurred = whenOccurred;
    }

    @Override
    public String toString() {
        return "AuditRecord{" +
                "changeMsg='" + changeMsg + '\'' +
                ", changedBy='" + changedBy + '\'' +
                ", whenOccurred='" + whenOccurred + '\'' +
                '}';
    }

    public String getChangeMsg() {
        return changeMsg;
    }

    public void setChangeMsg(String changeMsg) {
        this.changeMsg = changeMsg;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getWhenOccurred() {
        return whenOccurred;
    }

    public void setWhenOccurred(String whenOccurred) {
        this.whenOccurred = whenOccurred;
    }
}

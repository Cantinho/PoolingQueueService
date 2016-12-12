package br.com.pqs.bean;

/**
 * Created by jordaoesa on 25/11/16.
 */
public class Message {
    private String SerialNumber;
    private String ApplicationID;
    private String Timestamp;
    private String Priority;
    private String Message;

    public Message() {}

    public Message(String serialNumber, String applicationID, String timestamp, String priority, String message) {
        SerialNumber = serialNumber;
        ApplicationID = applicationID;
        Timestamp = timestamp;
        Priority = priority;
        Message = message;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        SerialNumber = serialNumber;
    }

    public String getApplicationID() {
        return ApplicationID;
    }

    public void setApplicationID(String applicationID) {
        ApplicationID = applicationID;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public String getPriority() {
        return Priority;
    }

    public void setPriority(String priority) {
        Priority = priority;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public static String parseToMinimalistString(final Message message) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(message.getSerialNumber() + ";");
        strBuilder.append(message.getApplicationID() + ";");
        strBuilder.append(message.getTimestamp() + ";");
        strBuilder.append(message.getPriority() + ";");
        strBuilder.append(message.getMessage());

        return strBuilder.toString();
    }

    public static Message parseMinimalistStringToMessage(final String message) throws Exception {
        String[] parts = message.split(";");
        if(parts.length == 5) {
            return new br.com.pqs.bean.Message(parts[0], parts[1], parts[2], parts[3], parts[4]);
        }
        throw new Exception("Fail to parse minimalist string to Message");
    }

}

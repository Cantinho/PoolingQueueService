package br.com.pqs.exceptions;

/**
 * Created by samirtf on 27/11/16.
 */
public class AmazonSQSException extends Exception {

    public static final int INVALID_CREDENTIALS = 500;

    private int code;

    public AmazonSQSException(String message) {
        super(message);
        this.code = -1;
    }

    public AmazonSQSException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(AmazonSQSException.class.getName() + ":[");
        strBuilder.append("message:" + getMessage() + ";");
        strBuilder.append("code:" + getCode());
        strBuilder.append("]");
        return strBuilder.toString();
    }
}

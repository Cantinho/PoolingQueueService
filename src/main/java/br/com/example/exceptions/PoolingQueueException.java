package br.com.example.exceptions;

import br.com.example.sqs.impl.PoolingQueue;

/**
 * Created by samirtf on 27/11/16.
 */
public class PoolingQueueException extends Exception {

    public static final int CENTRAL_NOT_FOUND = 400;
    public static final int APPLICATION_NOT_FOUND = 500;

    private int code;

    public PoolingQueueException(String message) {
        super(message);
        this.code = -1;
    }

    public PoolingQueueException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(PoolingQueueException.class.getName() + ":[");
        strBuilder.append("message:" + getMessage() + ";");
        strBuilder.append("code:" + getCode());
        strBuilder.append("]");
        return strBuilder.toString();
    }
}

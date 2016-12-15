package br.com.pqs.bean;

import br.com.processor.mapper.MessageMapper;
import org.springframework.http.HttpHeaders;

/**
 * Created by jordao on 15/12/16.
 */
public class PQSResponse {

    private HttpHeaders headers;
    private MessageMapper body;

    public PQSResponse() {
    }

    public PQSResponse(HttpHeaders headers, MessageMapper body) {
        this.headers = headers;
        this.body = body;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public MessageMapper getBody() {
        return body;
    }

    public void setBody(MessageMapper body) {
        this.body = body;
    }
}

package br.com.example;

import br.com.example.controllers.Controller;
import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class InitialPoint {

	private static Logger LOGGER = LoggerFactory.getLogger(InitialPoint.class);

	@Value("${server.http.port}")
	private Integer httpPort;

	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
		tomcat.addAdditionalTomcatConnectors(createStandardConnector());
		return tomcat;
	}

	private Connector createStandardConnector() {
		Connector connector = new Connector(TomcatEmbeddedServletContainerFactory.DEFAULT_PROTOCOL);
		connector.setPort(httpPort);
		return connector;
	}

	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		LOGGER.debug("Calling method: " + String.format("protected SpringApplicationBuilder configure( %s )", application.toString()));

		return application.sources(Controller.class);
	}

	public static void main(String[] args) throws Exception {
		LOGGER.debug("Calling method: " + String.format("public static void main( %s ) throws Exception", Arrays.toString(args)));

		SpringApplication.run(Controller.class, args);
	}
}

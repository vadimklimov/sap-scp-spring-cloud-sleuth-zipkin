package com.doc.scp.demo.trigger;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
public class TriggerController {

	@Value("${demo.sleep:1}")
	private int waitTime;

	@Value("${demo.randomizer.url:#{null}}")
	private String svcRandomizerUrl;

	@Autowired
	private Tracer tracer;

	@Autowired
	private RestTemplate restTemplate;

	private static final Logger LOG = LoggerFactory.getLogger(TriggerController.class);

	@RequestMapping(value = "/trigger", method = RequestMethod.GET)

	public ResponseEntity<Void> getTrigger() {

		LOG.info("Entering trigger service");

		Span span = tracer.getCurrentSpan();
		span.tag("demo-tag", "DemoTagValue");
		span.setBaggageItem("demo-baggage-item", "DemoBaggageItemValue");
		LOG.info("Trace ID: " + Span.idToHex(span.getTraceId()));
		LOG.info("Span ID: " + Span.idToHex(span.getSpanId()));
		span.getParents()
			.forEach((value) -> LOG.info("Parent span ID: " + Span.idToHex(value)));
		span.tags()
			.forEach((key, value) -> LOG.info("Span tag: " + key + " - " + value));
		span.getBaggage()
			.forEach((key, value) -> LOG.info("Span baggage item: " + key + " - " + value));

		try {
			LOG.info("Calling randomizer service");
			restTemplate.getForObject(new URI(svcRandomizerUrl), Void.class);

			long sleepTime = 1000 * waitTime;
			LOG.info("Thread is put to sleep for {} ms", sleepTime);
			Thread.sleep(sleepTime);
		} catch (URISyntaxException e) {
			LOG.error("Error when parsing URL of randomizer service", e);
		} catch (RestClientException e) {
			LOG.error("Error when calling randomizer service", e);
		} catch (InterruptedException e) {
			LOG.error("Error when putting thread to sleep", e);
		}

		LOG.info("Exiting trigger service");

		return ResponseEntity	.noContent()
								.build();

	}

}

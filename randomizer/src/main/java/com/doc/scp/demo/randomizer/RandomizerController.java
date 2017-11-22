package com.doc.scp.demo.randomizer;

import java.util.Random;

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

@RestController
public class RandomizerController {

	private static final Logger LOG = LoggerFactory.getLogger(RandomizerController.class);

	@Value("${demo.sleep.min:10}")
	private int waitTimeMin;

	@Value("${demo.sleep.max:30}")
	private int waitTimeMax;

	@Autowired
	private Tracer tracer;

	@RequestMapping(value = "/randomizer", method = RequestMethod.GET)
	public ResponseEntity<Void> getRandomizer() {

		LOG.info("Entering randomizer service");

		Span span = tracer.getCurrentSpan();
		LOG.info("Trace ID: " + Span.idToHex(span.getTraceId()));
		LOG.info("Span ID: " + Span.idToHex(span.getSpanId()));
		span.getParents()
			.forEach((value) -> LOG.info("Parent span ID: " + Span.idToHex(value)));
		span.tags()
			.forEach((key, value) -> LOG.info("Span tag: " + key + " - " + value));
		span.getBaggage()
			.forEach((key, value) -> LOG.info("Span baggage item: " + key + " - " + value));

		try {
			Random r = new Random();
			long sleepTime = 1000 * (r.nextInt((waitTimeMax - waitTimeMin) + 1) + waitTimeMin);
			LOG.info("Thread is put to sleep for {} ms", sleepTime);
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			LOG.error("Error when putting thread to sleep", e);
		}

		LOG.info("Exiting randomizer service");

		return ResponseEntity	.noContent()
								.build();

	}

}

package org.ironrhino.security.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class SimplePasswordChangedEventListener implements
		ApplicationListener<PasswordChangedEvent> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void onApplicationEvent(PasswordChangedEvent event) {
		logger.info(event.getUsername() + " changed password from {}",
				event.getRemoteAddr());
	}

}

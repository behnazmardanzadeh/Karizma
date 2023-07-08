package clinic.config.eventhandler;

import org.springframework.data.rest.core.event.RepositoryEvent;

public class AfterSaveEvent extends RepositoryEvent {
    public AfterSaveEvent(Object source) {
        super(source);
    }
}

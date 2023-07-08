package clinic.config.eventhandler;

import clinic.models.Appointment;
import clinic.models.Patient;
import clinic.services.email.SaveAppointmentEmailServiceImpl;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.rest.core.event.RepositoryEvent;

import java.time.format.DateTimeFormatter;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractRepositoryEventListener<T> implements ApplicationListener<RepositoryEvent> {
    private final Class<?> INTERESTED_TYPE = GenericTypeResolver.resolveTypeArgument(getClass(), AbstractRepositoryEventListener.class);

    @Autowired
    private SaveAppointmentEmailServiceImpl saveAppointmentEmailService;

    @Override
    public void onApplicationEvent(RepositoryEvent event) {
        Class<?> srcType = event.getSource().getClass();
        if (null != INTERESTED_TYPE && !INTERESTED_TYPE.isAssignableFrom(srcType)) {
            return;
        }
        if (event instanceof AfterSaveEvent) {
            onAfterSave((T) event.getSource());
        }
    }

    protected void onAfterSave(T entity) {
        if (entity instanceof Appointment) {
            log.info("onAfterSave : " + entity.toString());
        }
    }
}

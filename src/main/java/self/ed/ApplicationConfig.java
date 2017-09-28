package self.ed;

import org.glassfish.jersey.server.ResourceConfig;
import self.ed.resource.UserResource;

import javax.ws.rs.ApplicationPath;

/**
 * @author Anatolii
 */
@ApplicationPath("rest")
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        register(UserResource.class);
    }
}

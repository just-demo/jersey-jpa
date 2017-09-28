package self.ed.resource;

import self.ed.entity.User;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.function.Function;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;

/**
 * @author Anatolii
 */
@Path("/users")
@Singleton
public class UserResource {
    private EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("self-ed-persistence-unit");
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Response getAll() {
        List<User> users = doInTransaction(em -> em.createQuery("from User", User.class).getResultList());
        return Response.status(OK).entity(users).build();
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public Response get(@PathParam("id") Long id) {
        User user = doInTransaction(em -> em.find(User.class, id));
        if (user == null) {
            return Response.status(NOT_FOUND).build();
        }
        return Response.status(OK).entity(user).build();
    }

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response create(User user) {
        user.setId(null);
        doInTransaction(em -> {
            em.persist(user);
            return null;
        });
        return Response.status(CREATED).entity(user).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response update(@PathParam("id") Long id, User user) {
        user.setId(id);
        User updatedUser = doInTransaction(em -> em.find(User.class, id) != null ? em.merge(user) : null);

        if (updatedUser == null) {
            return Response.status(NOT_FOUND).build();
        }

        return Response.status(OK).entity(updatedUser).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        User deletedUser = doInTransaction(em -> {
            User user = em.find(User.class, id);
            if (user != null) {
                em.remove(user);
            }
            return user;
        });

        if (deletedUser == null) {
            return Response.status(NOT_FOUND).build();
        }

        return Response.status(NO_CONTENT).build();
    }

    private <T> T doInTransaction(Function<EntityManager, T> action) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        T result = action.apply(em);
        em.getTransaction().commit();
        em.close();
        return result;
    }
}
package org.juntos;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/hello-resteasy")
public class GreetingResource {

    @ConfigProperty(name = "mconfiguracion.mensaje", defaultValue = "nok8s")
    String mensaje;

    @ConfigProperty(name = "mconfiguracion.mensaje02", defaultValue = "nok8s")
    String mensaje02;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello RESTEasy";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("hola/{name}")
    public String hola(@PathParam("name") String name) {
        return "Hello siempre01 "+name+", mi mensaje: "+ mensaje;
    }
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("hola02/{name}")
    public String hola02(@PathParam("name") String name) {
        int a = 5 + 15;
        return "Hello 02 "+name+", mi mensaje: "+ mensaje02+" "+String.valueOf(a);
    }
}
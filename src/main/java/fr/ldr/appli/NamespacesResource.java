package fr.ldr.appli;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Path("/namespaces")
public class NamespacesResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        Config config = new ConfigBuilder().build();
        KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
        List<Namespace> namespaces = kubernetesClient.namespaces().list().getItems();
        for (Namespace namespace : namespaces) {
            System.out.println(namespace.getMetadata().getName());
        }
        return namespaces.stream().map(Namespace::getMetadata).map(ObjectMeta::getName).collect(Collectors.joining(" "));
    }

    @POST
    @Path("/create")
    @Produces(MediaType.TEXT_PLAIN)
    public Response create() {
        Config config = new ConfigBuilder().build();
        KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
        ObjectMeta objectMeta = new ObjectMetaBuilder().withClusterName("developppement").withName("mon-namespace").build();
        Namespace namespace = new NamespaceBuilder().withApiVersion("v1").withKind("Namespace").withMetadata(objectMeta).build();
        Namespace newNamespace = kubernetesClient.namespaces().create(namespace);

        return Response.created(URI.create("/api/v1/namespace/monnamespace")).entity(newNamespace).build();
    }
}

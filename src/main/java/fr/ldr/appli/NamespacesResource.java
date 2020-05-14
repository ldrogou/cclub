package fr.ldr.appli;

import io.fabric8.kubernetes.api.model.AuthInfo;
import io.fabric8.kubernetes.api.model.Cluster;
import io.fabric8.kubernetes.api.model.NamedAuthInfo;
import io.fabric8.kubernetes.api.model.NamedCluster;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Path("/namespaces")
public class NamespacesResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get() throws IOException {

        KubernetesClient kubernetesClient = new DefaultKubernetesClient(getConfig());

        List<Namespace> namespaces = kubernetesClient.namespaces()
                .list()
                .getItems();
        for (Namespace namespace : namespaces) {
            System.out.println(namespace.getMetadata()
                    .getName());
        }
        return namespaces.stream()
                .map(Namespace::getMetadata)
                .map(ObjectMeta::getName)
                .collect(Collectors.joining(" "));
    }



    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response create() throws IOException {

        KubernetesClient kubernetesClient = new DefaultKubernetesClient(getConfig());
        ObjectMeta objectMeta = new ObjectMetaBuilder().withClusterName("developppement")
                .withName("mon-namespace")
                .build();
        Namespace namespace = new NamespaceBuilder().withApiVersion("v1")
                .withKind("Namespace")
                .withMetadata(objectMeta)
                .build();
        Namespace newNamespace = kubernetesClient.namespaces()
                .create(namespace);

        return Response.created(URI.create("/api/v1/namespace/monnamespace"))
                .entity(newNamespace)
                .build();
    }

    private Config getConfig() throws IOException {

        io.fabric8.kubernetes.api.model.Config configUtils = KubeConfigUtils.parseConfig(new File("/home/ladro/.kube/config"));
        AuthInfo authInfo = configUtils.getUsers().stream().filter(namedAuthInfo -> namedAuthInfo.getName().startsWith("admin")).map(
                NamedAuthInfo::getUser).findFirst().get();
        Cluster cluster = configUtils.getClusters().stream().filter(namedCluster -> namedCluster.getName().endsWith("developpement")).map(
                NamedCluster::getCluster).findFirst().get();
        return new ConfigBuilder().withMasterUrl(cluster.getServer())
                .withTrustCerts(true)
                .withPassword(authInfo.getPassword())
                .withUsername(authInfo.getUsername())
                .build();
    }
}

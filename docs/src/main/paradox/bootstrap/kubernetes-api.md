# Kubernetes API

The Kubernetes API can be used to discover peers and form a Pekko Cluster. The `kubernetes-api`
mechanism queries the Kubernetes API server to find pods with a given label. A Kubernetes service isn't required
for the cluster bootstrap but may be used for external access to the application.

The following Kubernetes resources are created:

* Deployment: For creating the Pekko pods
* Role and RoleBinding to give the pods access to the API server

An example deployment (used for integration testing):

@@snip [pekko-cluster.yml](/integration-test/kubernetes-api/kubernetes/pekko-cluster.yml) { #deployment }

An example `Role` and `Rolebinding` to allow the nodes to query the Kubernetes API server:

@@snip [pekko-cluster.yml](/integration-test/kubernetes-api/kubernetes/pekko-cluster.yml) { #rbac }

The User name includes the namespace, this will need updated for your namespace.

The following configuration is required:

* Set `pekko.management.cluster.bootstrap.contact-point-discovery.discovery-method` to `kubernetes-api`
* Set `pekko.discovery.kubernetes-api.pod-label-selector` to a label selector that will match the Pekko pods e.g. `app=%s`

@@snip [pekko-cluster.yml](/integration-test/kubernetes-api/src/main/resources/application.conf) { #discovery-config }

The lookup needs to know which namespace to look in. By default, this will be detected by reading the namespace
from the service account secret, in `/var/run/secrets/kubernetes.io/serviceaccount/namespace`, but can be overridden by
setting `pekko.discovery.kubernetes-api.pod-namespace`.

For more details on how to configure the Kubernetes deployment see @ref:[recipes](recipes.md).


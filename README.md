# Develocity™ Test Distribution w/ Kubedock Example

This is an example of how to run tests using Develocity™ Test Distribution agents running in Kubernetes that dynamically provision Docker containers.
This is common with integration tests that use [Testcontainers](https://testcontainers.com/) to verify integration with databases or other external services.  

The solution leverages a tool called [kubedock](https://github.com/joyrex2001/kubedock), a tool to orchestrate containers on a Kubernetes cluster rather than Docker, running as a sidecar alongside the Test Distribution agents.

## Prerequisites

- Java 21 or later
- Access to a Kubernetes cluster
- A Develocity instance enabled with Test Distribution
- A Test Distribution agent pool and corresponding registration key

## Running the example

1. Export the Test Distribution registration key and pool ID as environment variables

```shell
export TEST_DISTRIBUTION_AGENT_REGISTRATION_KEY=«registration-key»
export TEST_DISTRIBUTION_AGENT_POOL_ID=«pool-id»
export DEVELOCITY_SERVER_URL=«server-url»
```

2. Deploy the Test Distribution agents with kubedock sidecars to the Kubernetes cluster

```shell
sed "s|«registration-key»|$TEST_DISTRIBUTION_AGENT_REGISTRATION_KEY|g" test-distribution-kubedock.yaml \
  | sed "s|«pool-id»|$TEST_DISTRIBUTION_AGENT_POOL_ID|g" \
  | sed "s|«server-url»|$DEVELOCITY_SERVER_URL|g" \
  | kubectl apply -f -
```

You will see:

```text
secret/test-distribution-agent-registration-key created
role.rbac.authorization.k8s.io/kubedock created
rolebinding.rbac.authorization.k8s.io/test-distribution-agent-kubedock-binding created
serviceaccount/test-distribution-agent created
deployment.apps/test-distribution-agent created
```

This will create a deployment with 2 replicas in the `default` namespace.

3. Verify the deployment was successful

```shell
kubectl get pods
```

You will see something similar to:

```text
NAME                                                  READY   STATUS    RESTARTS   AGE
test-distribution-agent-6c48d7c57f-67shq              2/2     Running   0          7s
test-distribution-agent-6c48d7c57f-b5gdk              2/2     Running   0          7s
```

To tear down the agents, run: 

```shell
kubectl delete -f test-distribution-kubedock.yaml
```

4. Watch the pods

In a new terminal, watch the pods.
When the example runs the tests in the next step, you will see the required pods being created and destroyed by kubedock.

```shell
kubectl get pods -w
```

Leave this terminal open.

5. Run the tests

The tests for the build located in the `example-testcontainers` directory rely on Testcontainers to start a PostgreSQL container.
They are configured to only use remote executors.

Before running the tests, you need to update the configured Develocity server in `example-testcontainers/settings.gradle.kts`.

```kotlin
develocity {
    server = "«server-url»"
}
```

Invoke the build to run the tests.

```shell
(cd example-testcontainers && ./gradlew build)
```

Switch to the terminal where you are watching the pods.
You will see the required pods being created and destroyed by kubedock.

```text
NAME                                                  READY   STATUS              RESTARTS   AGE
test-distribution-agent-6c48d7c57f-67shq              2/2     Running             0          14m
test-distribution-agent-6c48d7c57f-b5gdk              2/2     Running             0          14m
kubedock-0548814f1477                                 0/1     Pending             0          0s
kubedock-0548814f1477                                 0/1     Pending             0          0s
kubedock-0548814f1477                                 0/1     ContainerCreating   0          0s
kubedock-0548814f1477                                 1/1     Running             0          13s
kubedock-0548814f1477                                 1/1     Terminating         0          15s
kubedock-0548814f1477                                 0/1     Terminating         0          16s
kubedock-0548814f1477                                 0/1     Terminating         0          16s
kubedock-0548814f1477                                 0/1     Terminating         0          16s
kubedock-0548814f1477                                 0/1     Terminating         0          16s
kubedock-ef4b5a53dbab                                 0/1     Pending             0          0s
kubedock-ef4b5a53dbab                                 0/1     Pending             0          0s
kubedock-ef4b5a53dbab                                 0/1     ContainerCreating   0          0s
kubedock-1bd84bbcc604                                 0/1     Pending             0          0s
kubedock-1bd84bbcc604                                 0/1     Pending             0          0s
kubedock-1bd84bbcc604                                 0/1     ContainerCreating   0          0s
...
```

Inspect the build scan to verify the tests completed successfully and were executed on the remote executors.

Example: https://ge.solutions-team.gradle.com/s/riwlhhqtxvbyu/tests/overview

6. Run the tests again using only local executors

Configure the test suite in `example-testcontainers` to use only local executors.

```text
develocity {
    testDistribution {
        enabled = true
        // maxLocalExecutors = 0
        // maxRemoteExecutors = 2
        maxLocalExecutors = 2
        maxRemoteExecutors = 0
    }
}
```

Assuming a Docker environment is running and available, the tests will now use it instead. 

Inspect the build scan to verify the tests completed successfully and were executed on local executors.

Example: https://ge.solutions-team.gradle.com/s/ybspv2gjepfoi/tests/overview

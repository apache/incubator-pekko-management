#!/bin/bash

set -exu

export NAMESPACE=akka-bootstrap-demo-ns
export APP_NAME=akka-bootstrap-demo
export PROJECT_NAME=integration-test-kubernetes-api
export DEPLOYMENT=integration-test/kubernetes-api/kubernetes/pekko-cluster.yml

integration-test/scripts/kubernetes-test.sh

